(ns cljfx.lifecycle
  (:require [cljfx.component :as component]
            [cljfx.prop :as prop]
            [clojure.set :as set]))

(set! *warn-on-reflection* true)

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc opts] "Creates component")
  (advance [this component desc opts] "Advances component")
  (delete [this component opts] "Deletes component"))

(defn- desc->lifecycle [desc opts]
  (let [type (:fx/type desc)
        type->lifecycle (:fx.opt/type->lifecycle opts)]
    (or (type->lifecycle type)
        (throw (ex-info "Don't know how to get component lifecycle from :fx/type"
                        {:desc desc})))))

(defn- create-dynamic-component [lifecycle child-desc opts]
  (with-meta
    {:lifecycle lifecycle
     :child (create lifecycle child-desc opts)}
    {`component/instance #(-> % :child component/instance)}))

(def dynamic
  (with-meta
    [::dynamic]
    {`create (fn [_ desc opts]
               (let [lifecycle (desc->lifecycle desc opts)]
                 (create-dynamic-component lifecycle desc opts)))
     `advance (fn [_ component desc opts]
                (let [lifecycle (:lifecycle component)
                      new-lifecycle (desc->lifecycle desc opts)]
                  (if (identical? lifecycle new-lifecycle)
                    (-> component
                        (update :child #(advance lifecycle % desc opts)))
                    (do (delete lifecycle (:child component) opts)
                        (create-dynamic-component new-lifecycle desc opts)))))
     `delete (fn [_ component opts]
               (delete (:lifecycle component) (:child component) opts))}))

(def ^:dynamic *in-progress?* false)

(def root
  (with-meta
    [::root]
    {`create (fn [_ desc opts]
               (binding [*in-progress?* true]
                 (create dynamic desc opts)))
     `advance (fn [_ component desc opts]
                (binding [*in-progress?* true]
                  (advance dynamic component desc opts)))
     `delete (fn [_ component opts]
               (binding [*in-progress?* true]
                 (delete dynamic component opts)))}))

(defn- call-dynamic-fn [desc]
  ((:fx/type desc) (dissoc desc :fx/type)))

(defn wrap-dynamic-fn [lifecycle]
  (with-meta
    [::dynamic-fn lifecycle]
    {`create (fn [_ desc opts]
               (let [child-desc (call-dynamic-fn desc)]
                 (with-meta {:child-desc child-desc
                             :desc desc
                             :child (create lifecycle child-desc opts)}
                            {`component/instance #(-> % :child component/instance)})))
     `advance (fn [_ component desc opts]
                (if (= desc (:desc component))
                  (update component :child #(advance lifecycle
                                                     %
                                                     (:child-desc component)
                                                     opts))
                  (let [child-desc (call-dynamic-fn desc)]
                    (-> component
                        (assoc :child-desc child-desc :desc desc)
                        (update :child #(advance lifecycle % child-desc opts))))))
     `delete (fn [_ component opts]
               (delete lifecycle (:child component) opts))}))

(def dynamic-fn->dynamic
  (wrap-dynamic-fn dynamic))

(defn wrap-coerce [lifecycle coerce]
  (with-meta
    [::coerce lifecycle coerce]
    {`create (fn [_ desc opts]
               (let [child (create lifecycle desc opts)]
                 (with-meta {:child child
                             :value (coerce (component/instance child))}
                            {`component/instance :value})))
     `advance (fn [_ component desc opts]
                (let [child (:child component)
                      old-instance (component/instance child)
                      new-child (advance lifecycle child desc opts)
                      new-instance (component/instance new-child)]
                  (cond-> component
                    :always
                    (assoc :child new-child)

                    (not= old-instance new-instance)
                    (assoc :value (coerce new-instance)))))
     `delete (fn [_ component opts]
               (delete lifecycle (:child component) opts))}))

(def scalar
  (with-meta
    [::scalar]
    {`create (fn [_ v _] v)
     `advance (fn [_ _ v _] v)
     `delete (fn [_ _ _])}))

(defn- make-handler-fn [desc opts]
  (cond
    (map? desc)
    (let [map-event-handler (:fx.opt/map-event-handler opts)]
      #(when-not *in-progress?*
         (map-event-handler (assoc desc :fx/event %))))

    (fn? desc)
    #(when-not *in-progress?*
       (desc %))

    :else
    desc))

(defn- create-event-handler-component [desc opts]
  (with-meta {:desc desc
              :fx.opt/map-event-handler (:fx.opt/map-event-handler opts)
              :value (make-handler-fn desc opts)}
             {`component/instance :value}))

(def event-handler
  (with-meta
    [::event-handler]
    {`create (fn [_ desc opts]
               (create-event-handler-component desc opts))
     `advance (fn [_ component desc opts]
                (if (and (= desc (:desc component))
                         (= (:fx.opt/map-event-handler component)
                            (:fx.opt/map-event-handler opts)))
                  component
                  (create-event-handler-component desc opts)))
     `delete (fn [_ _ _])}))

(defn wrap-factory [lifecycle]
  (with-meta
    [::factory lifecycle]
    {`create (fn [_ desc opts]
               (with-meta
                 {:desc desc
                  :opts opts
                  :value (if (fn? desc)
                           #(component/instance (create lifecycle (desc %) opts))
                           desc)}
                 {`component/instance :value}))
     `advance (fn [this component desc opts]
                (if (and (= desc (:desc component))
                         (= opts (:opts opts)))
                  component
                  (create this desc opts)))
     `delete (fn [_ _ _])}))

(defn- ordered-keys+key->val
  "Return a vec of ordered calculated keys and a map of calculated keys to components

  Example:
  ```
  (ordered-keys+key->val [{:x 1}
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
  [key-fn coll]
  (loop [key-value->counter (transient {})
         keys (transient [])
         vals (transient {})
         xs (seq coll)]
    (if xs
      (let [[x & rest] xs
            key-value (key-fn x)
            key-index (key-value->counter key-value 0)
            key [key-value key-index]]
        (recur (assoc! key-value->counter key-value (inc key-index))
               (conj! keys key)
               (assoc! vals key x)
               rest))
      [(persistent! keys) (persistent! vals)])))

(defn- fx-key-from-map [desc]
  (:fx/key desc ::no-key))

(defn- strip-fx-key [desc]
  (dissoc desc :fx/key))

(defn wrap-many
  ([lifecycle]
   (wrap-many lifecycle fx-key-from-map strip-fx-key))
  ([lifecycle desc->key desc->child-desc]
   (with-meta
     [::many lifecycle desc->key desc->child-desc]
     {`create
      (fn [_ desc opts]
        (let [[ordered-keys key->desc] (ordered-keys+key->val desc->key desc)
              key->component (reduce (fn [acc key]
                                       (update acc key #(create lifecycle
                                                                (desc->child-desc %)
                                                                opts)))
                                     key->desc
                                     ordered-keys)]
          (with-meta
            {:instance (mapv #(-> % key->component component/instance) ordered-keys)
             :key->component key->component}
            {`component/instance :instance})))

      `advance
      (fn [_ component desc opts]
        (let [old-key->component (:key->component component)
              [ordered-keys key->desc] (ordered-keys+key->val desc->key desc)
              key->component (reduce
                               (fn [acc key]
                                 (let [old-e (find acc key)
                                       new-e (find key->desc key)]
                                   (cond
                                     (and (some? old-e) (some? new-e))
                                     (assoc acc key (advance lifecycle
                                                             (val old-e)
                                                             (desc->child-desc (val new-e))
                                                             opts))

                                     (some? old-e)
                                     (do (delete lifecycle (val old-e) opts)
                                         (dissoc acc key))

                                     :else
                                     (assoc acc key (create lifecycle
                                                            (desc->child-desc (val new-e))
                                                            opts)))))
                               old-key->component
                               (set (concat (keys old-key->component) ordered-keys)))]
          (assoc component
            :key->component key->component
            :instance (mapv #(-> % key->component component/instance) ordered-keys))))

      `delete (fn [_ component opts]
                (doseq [x (vals (:key->component component))]
                  (delete lifecycle x opts)))})))

(def dynamics
  (wrap-many dynamic))

(defn wrap-log [lifecycle log-fn]
  (with-meta
    [::log lifecycle log-fn]
    {`create (fn [_ desc opts]
               (log-fn `create desc)
               (let [child (create lifecycle desc opts)]
                 (with-meta {:child child
                             :desc desc}
                            {`component/instance #(-> % :child component/instance)})))
     `advance (fn [_ component desc opts]
                (log-fn `advance (:desc component) desc)
                (update component :child #(advance lifecycle % desc opts)))
     `delete (fn [_ component opts]
               (log-fn `delete (:desc component))
               (delete lifecycle (:child component) opts))}))

(defn wrap-extra-props [lifecycle props-config]
  (let [prop-key-set (set (keys props-config))]
    (with-meta
      [::pane-child-node lifecycle props-config]
      {`create
       (fn [_ desc opts]
         (let [child-desc (apply dissoc desc prop-key-set)
               child (create lifecycle child-desc opts)
               instance (component/instance child)
               prop-desc (select-keys desc prop-key-set)
               props (reduce
                       (fn [acc k]
                         (assoc acc k (create (prop/lifecycle (get props-config k))
                                              (get prop-desc k)
                                              opts)))
                       prop-desc
                       (keys prop-desc))]
           (doseq [[k v] props]
             (prop/assign! (get props-config k) instance v))
           (with-meta {:child child
                       :props props}
                      {`component/instance #(-> % :child component/instance)})))

       `advance
       (fn [_ component desc opts]
         (let [child (:child component)
               instance (component/instance child)
               child-desc (apply dissoc desc prop-key-set)
               new-child (advance lifecycle child child-desc opts)
               new-instance (component/instance new-child)
               with-child (assoc component :child new-child)
               props-desc (select-keys desc prop-key-set)]
           (if (identical? instance new-instance)
             (update
               with-child
               :props
               (fn [props]
                 (reduce
                   (fn [acc k]
                     (let [old-e (find props k)
                           new-e (find props-desc k)]
                       (cond
                         (and (some? old-e) (some? new-e))
                         (let [old-component (val old-e)
                               desc (val new-e)
                               prop-config (get props-config k)
                               new-component (advance (prop/lifecycle prop-config)
                                                      old-component
                                                      desc
                                                      opts)]
                           (prop/replace! prop-config
                                          instance
                                          old-component
                                          new-component)
                           (assoc acc k new-component))

                         (some? old-e)
                         (let [prop-config (get props-config k)]
                           (prop/retract! prop-config instance (val old-e))
                           (delete (prop/lifecycle prop-config) (val old-e) opts)
                           (dissoc acc k))

                         :else
                         (let [prop-config (get props-config k)
                               component (create (prop/lifecycle prop-config)
                                                 (val new-e)
                                                 opts)]
                           (prop/assign! prop-config instance component)
                           (assoc acc k component)))))
                   props
                   (set/union (set (keys props))
                              (set (keys props-desc))))))
             (do
               (doseq [[k v] props-desc]
                 (prop/assign! (get props-config k) new-instance v))
               (assoc with-child :props props-desc)))))

       `delete
       (fn [_ component opts]
         (doseq [[k v] (:props component)]
           (delete (prop/lifecycle (get props-config k)) v opts))
         (delete lifecycle (:child component) opts))})))

(defn wrap-on-delete [lifecycle f]
  (with-meta
    [::on-delete lifecycle f]
    {`create (fn [_ desc opts]
               (create lifecycle desc opts))
     `advance (fn [_ component desc opts]
                (advance lifecycle component desc opts))
     `delete (fn [_ component opts]
               (delete lifecycle component opts)
               (f (component/instance component)))}))

(defn- inject-context [desc opts]
  (assoc desc :fx/context (::context opts)))

(def dynamic-fn-with-context->dynamic
  (with-meta
    [::dynamic-fn-with-context->dynamic]
    {`create (fn [_ desc opts]
               (create dynamic-fn->dynamic (inject-context desc opts) opts))
     `advance (fn [_ component desc opts]
                (advance dynamic-fn->dynamic component (inject-context desc opts) opts))
     `delete (fn [_ component opts]
               (delete dynamic-fn->dynamic component opts))}))

(defn wrap-desc-as-context [lifecycle]
  (with-meta
    [::context lifecycle]
    {`create (fn [_ desc opts]
               (create lifecycle desc (assoc opts ::context desc)))
     `advance (fn [_ component desc opts]
                (advance lifecycle component desc (assoc opts ::context desc)))
     `delete (fn [_ component opts]
               (delete lifecycle component opts))}))

(defn wrap-map-desc [lifecycle f & args]
  (with-meta
    [::map-desc lifecycle f]
    {`create (fn [_ desc opts]
               (create lifecycle (apply f desc args) opts))
     `advance (fn [_ component desc opts]
                (advance lifecycle component (apply f desc args) opts))
     `delete (fn [_ component opts]
               (delete lifecycle component opts))}))