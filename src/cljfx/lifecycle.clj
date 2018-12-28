(ns cljfx.lifecycle
  (:require [cljfx.component :as component]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]
            [clojure.set :as set])
  (:import [javafx.scene Node]))

(set! *warn-on-reflection* true)

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

(defn wrap-fn-hiccup [lifecycle]
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
               (delete lifecycle (:child component) opts))}))

(def fn-dynamic-hiccup
  (wrap-fn-hiccup dynamic-hiccup))

(defn wrap-coerce [lifecycle coerce]
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
                 {`component/description :desc
                  `component/instance :value}))
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

(defn key-from-meta [desc]
  (:key (meta desc) ::no-key))

(defn wrap-many
  ([lifecycle]
   (wrap-many lifecycle key-from-meta))
  ([lifecycle desc->key]
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
  (wrap-many dynamic-hiccup))

(defn wrap-log [lifecycle log-fn]
  (with-meta
    [::log lifecycle log-fn]
    {`create (fn [_ desc opts]
               (log-fn `create desc)
               (create lifecycle desc opts))
     `advance (fn [_ component desc opts]
                (log-fn `advance (component/description component) desc)
                (advance lifecycle component desc opts))
     `delete (fn [_ component opts]
               (log-fn `delete (component/description component))
               (delete lifecycle component opts))}))

(defn- constraint-mutator
  "This mutator re-implements javafx.scene.layout.Pane/setConstraint (which is internal)"
  [constraint]
  (mutator/setter
    (fn [^Node node value]
      (let [properties (.getProperties node)
            parent (.getParent node)]
        (if (nil? value)
          (.remove properties constraint)
          (.put properties constraint value))
        (when parent
          (.requestLayout parent))))))

(defn- constraint+coerce->prop [[constraint coerce]]
  (prop/make (constraint-mutator constraint) scalar :coerce coerce))

(defn wrap-meta-constraints [lifecycle kw->constraint+coerce]
  (let [props-config (->> kw->constraint+coerce
                          (map (juxt key #(-> % val constraint+coerce->prop)))
                          (into {}))
        prop-key-set (set (keys props-config))]
    (with-meta
      [::pane-child-node lifecycle kw->constraint+coerce]
      {`create
       (fn [_ desc opts]
         (let [child (create lifecycle desc opts)
               node (component/instance child)
               prop-desc (select-keys (meta desc) prop-key-set)
               props (reduce
                       (fn [acc k]
                         (assoc acc k (create (prop/lifecycle (get props-config k))
                                              (get prop-desc k)
                                              opts)))
                       prop-desc
                       (keys prop-desc))]
           (doseq [[k v] props]
             (prop/assign! (get props-config k) node v))
           (with-meta {:child child
                       :desc desc
                       :props props}
                      {`component/description :desc
                       `component/instance #(-> % :child component/instance)})))

       `advance
       (fn [_ component desc opts]
         (let [child (:child component)
               instance (component/instance child)
               new-child (advance lifecycle child desc opts)
               new-instance (component/instance new-child)
               with-child (assoc component :child new-child)
               props-desc (select-keys (meta desc) prop-key-set)]
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
