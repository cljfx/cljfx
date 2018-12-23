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
  (assign [this instance value opts])
  (replace* [this instance old-value new-value opts])
  (retract [this instance value opts])
  ;; transforms
  (coerce [this value opts])
  (ident [this value] "If value returned by ident changes during replace, mutating should happen"))

(defn prop [mut val & {:as opts}]
  (with-meta (or opts {})
             (merge mut val)))

(defn replace [this instance old-value new-value opts]
  (when-not (= (ident this old-value) (ident this new-value))
    (replace* this instance old-value new-value opts)))

(defn setter [setter-fn]
  {`assign (fn [this instance value opts]
             (setter-fn instance (coerce this value opts)))
   `replace* (fn [this instance _ new-value opts]
               (setter-fn instance (coerce this new-value opts)))
   `retract (fn [this instance _ opts]
              (if-let [e (find this :default)]
                (setter-fn instance (coerce this (val e) opts))
                (setter-fn instance nil)))})

(defn adder-remover [add-fn remove-fn]
  {`assign (fn [this instance value opts]
             (add-fn instance (coerce this value opts)))
   `replace* (fn [this instance old-value new-value opts]
               (remove-fn instance (coerce this old-value opts))
               (add-fn instance (coerce this new-value opts)))
   `retract (fn [this instance value opts]
              (remove-fn instance (coerce this value opts)))})

(defn property-change-listener [get-property-fn]
  (adder-remover
    #(.addListener ^ObservableValue (get-property-fn %1) ^ChangeListener %2)
    #(.removeListener ^ObservableValue (get-property-fn %1) ^ChangeListener %2)))

(defn ctor-only []
  {`assign (fn [_ _ v _]
             (throw (ex-info "Can't assign ctor arg" {:v v})))
   `replace* (fn [_ _ _ v _]
               (throw (ex-info "Can't replace ctor arg" {:v v})))
   `retract (fn [_ _ v _]
              (throw (ex-info "Can't retract ctor arg" {:v v})))})

(defn observable-list [get-list-fn]
  {`assign (fn [this instance value opts]
             (.setAll ^ObservableList (get-list-fn instance)
                      ^Collection (coerce this value opts)))
   `replace* (fn [this instance _ new-value opts]
               (.setAll ^ObservableList (get-list-fn instance)
                        ^Collection (coerce this new-value opts)))
   `retract (fn [_ instance _ _]
              (.clear ^ObservableList instance))})

(defn observable-map [get-map-fn]
  {`assign (fn [this instance value opts]
             (let [m ^ObservableMap (get-map-fn instance)]
               (.clear m)
               (.putAll m (coerce this value opts))))
   `replace* (fn [this instance _ new-value opts]
               (let [m ^ObservableMap (get-map-fn instance)]
                 (.clear m)
                 (.putAll m (coerce this new-value opts))))
   `retract (fn [_ instance _ _]
              (.clear ^ObservableMap (get-map-fn instance)))})

(defn extract-single [args]
  (if-not (= 1 (count args))
    (throw (ex-info "Should have exactly one arg"
                    {:args args})))
  (first args))

(defn extract-all [args]
  args)

(defn- identity-coerce [v _]
  v)

(def scalar
  {`lifecycle/create (fn [_ v _] v)
   `lifecycle/advance (fn [_ _ v _] v)
   `lifecycle/delete (fn [_ _ _])
   `coerce (fn [this v opts] ((:coerce this identity-coerce) v opts))
   `ident (fn [_ v] v)})

(def instance
  {`lifecycle/create (fn [this desc opts]
                       {:desc desc
                        :instance ((:coerce this identity-coerce) desc opts)})
   `lifecycle/advance (fn [this old-value new-desc opts]
                        (if (= (:desc old-value) new-desc)
                          old-value
                          {:desc new-desc
                           :instance ((:coerce this identity-coerce) new-desc opts)}))
   `lifecycle/delete (fn [_ _ _])
   `coerce (fn [_ value _]
             (:instance value))
   `ident (fn [_ value]
            (:desc value))})

(def component
  {`lifecycle/create (fn [_ desc opts]
                       (lifecycle/create-component desc opts))
   `lifecycle/advance (fn [_ component new-desc opts]
                        (lifecycle/advance-component component new-desc opts))
   `lifecycle/delete (fn [_ component opts]
                       (lifecycle/delete-component component opts))
   `ident (fn [_ component]
            (component/instance component))
   `coerce (fn [_ component _]
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
   (fn [_ descs opts]
     (let [components (mapv #(lifecycle/create-component % opts) descs)
           [_ key->component] (ordered-keys+key->component components component->key)]
       {:components components
        :key->component key->component}))

   `lifecycle/advance
   (fn [_ component-vec new-descs opts]
     (let [key->component (:key->component component-vec)
           [ordered-keys key->descs] (ordered-keys+key->component new-descs desc->key)
           new-key->component (reduce
                                (fn [acc key]
                                  (let [old-e (find key->component key)
                                        new-e (find key->descs key)]
                                    (cond
                                      (and (some? old-e) (some? new-e))
                                      (assoc acc key (lifecycle/advance-component
                                                       (val old-e)
                                                       (val new-e)
                                                       opts))

                                      (some? old-e)
                                      (do (lifecycle/delete-component (val old-e) opts)
                                          (dissoc acc key))

                                      :else
                                      (assoc acc key (lifecycle/create-component
                                                       (val new-e)
                                                       opts)))))
                                key->component
                                (set (concat (keys key->component) (keys key->descs))))]
       {:components (mapv new-key->component ordered-keys)
        :key->component new-key->component}))

   `lifecycle/delete
   (fn [_ component-vec opts]
     (doseq [x (:components component-vec)]
       (lifecycle/delete-component x opts)))

   `coerce
   (fn [_ value _]
     (component-vec->instances value))

   `ident
   (fn [_ value]
     (component-vec->instances value))})