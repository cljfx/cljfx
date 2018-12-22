(ns cljfx.prop
  (:refer-clojure :exclude [replace])
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.component :as component])
  (:import [javafx.collections ObservableList ObservableMap]
           [java.util Collection]
           [javafx.beans.value ObservableValue ChangeListener]))

(set! *warn-on-reflection* true)

(defprotocol Prop
  :extend-via-metadata true
  ;; key lifecycle
  (assign [this instance value])
  (replace* [this instance old-value new-value])
  (retract [this instance value])
  ;; transforms
  (coerce [this value])
  (ident [this value] "If value returned by ident changes during replace, mutating should happen"))

(defn prop [mut val & {:as opts}]
  (with-meta (or opts {})
             (merge mut val)))

(defn replace [this instance old-value new-value]
  (when-not (= (ident this old-value) (ident this new-value))
    (replace* this instance old-value new-value)))

(defn setter [setter-fn]
  {`assign (fn [this instance value]
             (setter-fn instance (coerce this value)))
   `replace* (fn [this instance _ new-value]
               (setter-fn instance (coerce this new-value)))
   `retract (fn [this instance _]
              (if-let [e (find this :default)]
                (setter-fn instance (coerce this (val e)))
                (setter-fn instance nil)))})

(defn adder-remover [add-fn remove-fn]
  {`assign (fn [this instance value]
             (add-fn instance (coerce this value)))
   `replace* (fn [this instance old-value new-value]
               (remove-fn instance (coerce this old-value))
               (add-fn instance (coerce this new-value)))
   `retract (fn [this instance value]
              (remove-fn instance (coerce this value)))})

(defn property-change-listener [get-property-fn]
  (adder-remover
    #(.addListener ^ObservableValue (get-property-fn %1) ^ChangeListener %2)
    #(.removeListener ^ObservableValue (get-property-fn %1) ^ChangeListener %2)))

(defn ctor-only []
  {`assign (fn [_ _ v]
             (throw (ex-info "Can't assign ctor arg" {:v v})))
   `replace* (fn [_ _ _ v]
               (throw (ex-info "Can't replace ctor arg" {:v v})))
   `retract (fn [_ _ v]
              (throw (ex-info "Can't retract ctor arg" {:v v})))})

(defn observable-list [get-list-fn]
  {`assign (fn [this instance value]
             (.setAll ^ObservableList (get-list-fn instance)
                      ^Collection (coerce this value)))
   `replace* (fn [this instance _ new-value]
               (.setAll ^ObservableList (get-list-fn instance)
                        ^Collection (coerce this new-value)))
   `retract (fn [_ instance _]
              (.clear ^ObservableList instance))})

(defn observable-map [get-map-fn]
  {`assign (fn [this instance value]
             (let [m ^ObservableMap (get-map-fn instance)]
               (.clear m)
               (.putAll m (coerce this value))))})

(defn extract-single [args]
  (if-not (= 1 (count args))
    (throw (ex-info "Should have exactly one arg"
                    {:args args})))
  (first args))

(defn extract-all [args]
  args)

(def scalar
  {`lifecycle/create (fn [_ v] v)
   `lifecycle/advance (fn [_ _ v] v)
   `lifecycle/delete (fn [_ _])
   `coerce (fn [this v] ((:coerce this identity) v))
   `ident (fn [_ v] v)})

(def instance
  {`lifecycle/create (fn [this desc]
                       {:desc desc
                        :instance ((:coerce this) desc)})
   `lifecycle/advance (fn [this old-value new-desc]
                        (if (= (:desc old-value) new-desc)
                          old-value
                          {:desc new-desc
                           :instance ((:coerce this) new-desc)}))
   `lifecycle/delete (fn [_ _])
   `coerce (fn [_ value]
             (:instance value))
   `ident (fn [_ value]
            (:desc value))})

(def component
  {`lifecycle/create (fn [_ desc]
                       (lifecycle/create-component desc))
   `lifecycle/advance (fn [_ component new-desc]
                        (lifecycle/advance-component component new-desc))
   `lifecycle/delete (fn [_ component]
                       (lifecycle/delete-component component))
   `ident (fn [_ component]
            (component/instance component))
   `coerce (fn [_ component]
             (component/instance component))})

(defn- ordered-keys+key->component
  "Return a vec of ordered calculated keys and a map of calculated keys to components

  Example:
  ```
  (ordered-keys+key->component [{:x 1}
                                (with-meta {:key 1} {:key 1})
                                (with-meta {:also 1} {:key 1})
                                {}]
                               #(-> % meta (get :key ::no-key)))
  => [[[::no-key 0]
       [1 0]
       [1 1]
       [::no-key 1]]
      {[::no-key 0] {:x 1},
       [1 0] {:key 1},
       [1 1] {:also 1},
       [::no-key 1] {}}]
  ```"
  [components key-fn]
  (loop [key->component (transient {})
         index->key (transient [])
         component-key->index (transient {})
         [x & xs] components]
    (let [key-value (key-fn x)
          key-index (component-key->index key-value 0)
          key [key-value key-index]
          new-key->component (assoc! key->component key x)
          new-index->key (conj! index->key key)]
      (if xs
        (recur new-key->component
               new-index->key
               (assoc! component-key->index key-value (inc key-index))
               xs)
        [(persistent! new-index->key) (persistent! new-key->component)]))))

(defn- desc->key [desc]
  (-> desc meta (get :key ::no-key)))

(defn- component->key [component]
  (-> component meta :cljfx/desc desc->key))

(defn- component-vec->instances [component-vec]
  (into []
        (comp (map component/instance)
              (remove nil?))
        (:components component-vec)))

(def component-vec
  {`lifecycle/create
   (fn [_ descs]
     (let [components (mapv lifecycle/create-component descs)
           [_ key->component] (ordered-keys+key->component components component->key)]
       {:components components
        :key->component key->component}))

   `lifecycle/advance
   (fn [_ component-vec new-descs]
     (let [key->component (:key->component component-vec)
           [ordered-keys key->descs] (ordered-keys+key->component new-descs desc->key)
           new-key->component (reduce
                                (fn [acc key]
                                  (let [old-e (find key->component key)
                                        new-e (find key->descs key)]
                                    (cond
                                      (and (some? old-e) (some? new-e))
                                      (assoc acc key (lifecycle/advance-component (val old-e) (val new-e)))

                                      (some? old-e)
                                      (do (lifecycle/delete-component (val old-e))
                                          (dissoc acc key))

                                      :else
                                      (assoc acc key (lifecycle/create-component (val new-e))))))
                                key->component
                                (set (concat (keys key->component) (keys key->descs))))]
       {:components (mapv new-key->component ordered-keys)
        :key->component new-key->component}))

   `lifecycle/delete
   (fn [_ component-vec]
     (doseq [x (:components component-vec)]
       (lifecycle/delete-component x)))

   `coerce
   (fn [_ value]
     (component-vec->instances value))

   `ident
   (fn [_ value]
     (component-vec->instances value))})