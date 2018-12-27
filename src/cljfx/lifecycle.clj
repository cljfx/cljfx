(ns cljfx.lifecycle
  (:require [cljfx.component :as component]))

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc opts] "Creates component")
  (advance [this component desc opts] "Advances component")
  (delete [this component opts] "Deletes component"))

(defn- desc->lifecycle [desc opts]
  (let [tag (nth desc 0)
        tag->lifecycle (:cljfx.opt/tag->lifecycle opts)]
    (or (tag->lifecycle tag)
        (throw (ex-info "Don't know how to get component lifecycle from tag"
                        {:tag tag})))))

(defn- create-dynamic-component [lifecycle desc opts]
  (with-meta
    {:lifecycle lifecycle
     :child (create lifecycle desc opts)
     :desc desc}
    {`component/description :desc
     `component/instance #(-> % :child component/instance)}))

(def dynamic-hiccup
  (with-meta
    [::dynamic-hiccup]
    {`create (fn [_ desc opts]
               (let [lifecycle (desc->lifecycle desc opts)]
                 (create-dynamic-component lifecycle desc opts)))
     `advance (fn [_ component desc opts]
                (let [lifecycle (:lifecycle component)
                      new-lifecycle (desc->lifecycle desc opts)]
                  (if (identical? lifecycle new-lifecycle)
                    (-> component
                        (update :child #(advance lifecycle % desc opts))
                        (assoc :desc desc))
                    (do (delete lifecycle (:child component) opts)
                        (create-dynamic-component new-lifecycle desc opts)))))
     `delete (fn [_ component opts]
               (delete (:lifecycle component) (:child component) opts))}))

(defn wrap-fn-hiccup []
  (fn [lifecycle]
    (with-meta
      [::fn-hiccup lifecycle]
      {`create (fn [_ [f & args :as desc] opts]
                 (let [child-desc (apply f args)]
                   (with-meta {:desc desc
                               :child-desc child-desc
                               :child (create lifecycle child-desc opts)}
                              {`component/description :desc
                               `component/instance #(-> % :child component/instance)})))
       `advance (fn [_ component [f & args :as desc] opts]
                  (if (= args (:args component))
                    (update component :child #(advance lifecycle
                                                       %
                                                       (:child-desc component)
                                                       opts))
                    (let [child-desc (apply f args)]
                      (-> component
                          (assoc :child-desc child-desc :desc desc)
                          (update :child #(advance lifecycle % child-desc opts))))))
       `delete (fn [_ component opts]
                 (delete lifecycle (:child component) opts))})))

(def fn-dynamic-hiccup
  ((wrap-fn-hiccup) dynamic-hiccup))

(defn wrap-coerce [coerce]
  (fn [lifecycle]
    (with-meta
      [::coerce lifecycle coerce]
      {`create (fn [_ desc opts]
                 (let [child (create lifecycle desc opts)]
                   (with-meta {:child child
                               :value (coerce (component/instance child))}
                              {`component/description #(-> % :child component/description)
                               `component/instance :value})))
       `advance (fn [_ component desc opts]
                  (let [child (:child component)
                        new-child (advance lifecycle child desc opts)]
                    (cond-> component
                      :always
                      (assoc :child new-child)

                      (not= (component/instance child) (component/instance new-child))
                      (assoc :value (coerce (component/instance new-child))))))
       `delete (fn [_ component opts]
                 (delete lifecycle (:child component) opts))})))

(def scalar
  (with-meta
    [::scalar]
    {`create (fn [_ v _] v)
     `advance (fn [_ _ v _] v)
     `delete (fn [_ _ _])}))

(defn- make-handler-fn [desc opts]
  (if (map? desc)
    (let [f (:cljfx.opt/map-event-handler opts)]
      #(f (assoc desc :cljfx/event %)))
    desc))

(defn- create-event-handler-component [desc opts]
  (with-meta {:desc desc
              :cljfx.opt/map-event-handler (:cljfx.opt/map-event-handler opts)
              :value (make-handler-fn desc opts)}
             {`component/description :desc
              `component/instance :value}))

(def event-handler
  (with-meta
    [::event-handler]
    {`create (fn [_ desc opts]
               (create-event-handler-component desc opts))
     `advance (fn [_ component desc opts]
                (if (and (= desc (:desc component))
                         (= (:cljfx.opt/map-event-handler component)
                            (:cljfx.opt/map-event-handler opts)))
                  component
                  (create-event-handler-component desc opts)))
     `delete (fn [_ _ _])}))

(defn- ordered-keys+key->val
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
  [vals key-fn]
  (loop [key->component (transient {})
         index->key (transient [])
         component-key->index (transient {})
         [x & xs] vals]
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

(defn wrap-many [desc->key]
  (fn [lifecycle]
    (with-meta
      [::many lifecycle desc->key]
      {`create
       (fn [_ desc opts]
         (let [components (mapv #(create lifecycle % opts) desc)
               [_ key->component] (ordered-keys+key->val
                                    components
                                    #(-> % component/description desc->key))]
           (with-meta {:components components
                       :desc desc
                       :key->component key->component}
                      {`component/description :desc
                       `component/instance #(->> %
                                                 :components
                                                 (mapv component/instance))})))

       `advance
       (fn [_ component desc opts]
         (let [key->component (:key->component component)
               [ordered-keys key->desc] (ordered-keys+key->val desc desc->key)
               new-key->component (reduce (fn [acc key]
                                            (let [old-e (find key->component key)
                                                  new-e (find key->desc key)]
                                              (cond
                                                (and (some? old-e) (some? new-e))
                                                (assoc acc key (advance lifecycle
                                                                        (val old-e)
                                                                        (val new-e)
                                                                        opts))

                                                (some? old-e)
                                                (do (delete lifecycle (val old-e) opts)
                                                    (dissoc acc key))

                                                :else
                                                (assoc acc key (create lifecycle
                                                                       (val new-e)
                                                                       opts)))))
                                          key->component
                                          (set (concat (keys key->component)
                                                       (keys key->desc))))]
           (assoc component :key->component new-key->component
                            :desc desc
                            :components (mapv new-key->component ordered-keys))))

       `delete (fn [_ component opts]
                 (doseq [x (:components component)]
                   (delete lifecycle x opts)))})))

(def many-dynamic-hiccups
  ((wrap-many #(-> % meta (get :key ::no-key))) dynamic-hiccup))

(defn wrap-log [log-fn]
  (fn [lifecycle]
    (with-meta
      [::wrap-log lifecycle log-fn]
      {`create (fn [_ desc opts]
                 (log-fn `create desc)
                 (create lifecycle desc opts))
       `advance (fn [_ component desc opts]
                  (log-fn `advance (component/description component) desc)
                  (advance lifecycle component desc opts))
       `delete (fn [_ component opts]
                 (log-fn `delete (component/description component))
                 (delete lifecycle component opts))})))