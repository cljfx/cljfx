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

(defn- create-hiccup-component [lifecycle desc opts]
  (with-meta
    {:lifecycle lifecycle
     :child (create lifecycle desc opts)}
    {`component/instance #(-> % :child component/instance)}))

(def hiccup
  (with-meta
    [::hiccup]
    {`create (fn [_ desc opts]
               (let [lifecycle (desc->lifecycle desc opts)]
                 (create-hiccup-component lifecycle desc opts)))
     `advance (fn [_ component desc opts]
                (let [lifecycle (:lifecycle component)
                      new-lifecycle (desc->lifecycle desc opts)]
                  (if (identical? lifecycle new-lifecycle)
                    (-> component
                        (update :child #(advance lifecycle % desc opts)))
                    (do (delete lifecycle (:child component) opts)
                        (create-hiccup-component new-lifecycle desc opts)))))
     `delete (fn [_ component opts]
               (delete (:lifecycle component) (:child component) opts))}))

(defn wrap-hiccup-fn [lifecycle]
  (with-meta
    [::hiccup-fn lifecycle]
    {`create (fn [_ [f & args] opts]
               (let [child-desc (apply f args)]
                 (with-meta {:child-desc child-desc
                             :args args
                             :child (create lifecycle child-desc opts)}
                            {`component/instance #(-> % :child component/instance)})))
     `advance (fn [_ component [f & args] opts]
                (if (= args (:args component))
                  (update component :child #(advance lifecycle
                                                     %
                                                     (:child-desc component)
                                                     opts))
                  (let [child-desc (apply f args)]
                    (-> component
                        (assoc :child-desc child-desc :args args)
                        (update :child #(advance lifecycle % child-desc opts))))))
     `delete (fn [_ component opts]
               (delete lifecycle (:child component) opts))}))

(def hiccup-fn->hiccup
  (wrap-hiccup-fn hiccup))

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
  (if (map? desc)
    (let [f (:cljfx.opt/map-event-handler opts)]
      #(f (assoc desc :cljfx/event %)))
    desc))

(defn- create-event-handler-component [desc opts]
  (with-meta {:desc desc
              :cljfx.opt/map-event-handler (:cljfx.opt/map-event-handler opts)
              :value (make-handler-fn desc opts)}
             {`component/instance :value}))

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

(defn- key-from-meta [desc]
  (:key (meta desc) ::no-key))

(defn wrap-many
  ([lifecycle]
   (wrap-many lifecycle key-from-meta))
  ([lifecycle key-fn]
   (with-meta
     [::many lifecycle key-fn]
     {`create
      (fn [_ desc opts]
        (let [[ordered-keys key->desc] (ordered-keys+key->val key-fn desc)
              key->component (reduce (fn [acc key]
                                       (update acc key #(create lifecycle % opts)))
                                     key->desc
                                     ordered-keys)]
          (with-meta
            {:instance (mapv #(-> % key->component component/instance) ordered-keys)
             :key->component key->component}
            {`component/instance :instance})))

      `advance
      (fn [_ component desc opts]
        (let [old-key->component (:key->component component)
              [ordered-keys key->desc] (ordered-keys+key->val key-fn desc)
              key->component (reduce
                               (fn [acc key]
                                 (let [old-e (find acc key)
                                       new-e (find key->desc key)]
                                   (cond
                                     (and (some? old-e) (some? new-e))
                                     (assoc acc key (advance lifecycle (val old-e) (val new-e) opts))

                                     (some? old-e)
                                     (do (delete lifecycle (val old-e) opts)
                                         (dissoc acc key))

                                     :else
                                     (assoc acc key (create lifecycle (val new-e) opts)))))
                               old-key->component
                               (set (concat (keys old-key->component) ordered-keys)))]
          (assoc component
            :key->component key->component
            :instance (mapv #(-> % key->component component/instance) ordered-keys))))

      `delete (fn [_ component opts]
                (doseq [x (vals (:key->component component))]
                  (delete lifecycle x opts)))})))

(def hiccups
  (wrap-many hiccup))

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
                       :props props}
                      {`component/instance #(-> % :child component/instance)})))

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

(defn- inject-context [f args opts]
  (apply vector f (::context opts) args))

(def hiccup-fn-with-context->hiccup
  (with-meta
    [::hiccup-fn-with-context->hiccup]
    {`create (fn [_ [f & args] opts]
               (create hiccup-fn->hiccup (inject-context f args opts) opts))
     `advance (fn [_ component [f & args] opts]
                (advance hiccup-fn->hiccup component (inject-context f args opts) opts))
     `delete (fn [_ component opts]
               (delete hiccup-fn->hiccup component opts))}))

(defn wrap-desc-as-context [lifecycle]
  (with-meta
    [::context lifecycle]
    {`create (fn [_ desc opts]
               (create lifecycle desc (assoc opts ::context desc)))
     `advance (fn [_ component desc opts]
                (advance lifecycle component desc (assoc opts ::context desc)))
     `delete (fn [_ component opts]
               (delete lifecycle component opts))}))

(defn wrap-map-desc [lifecycle f]
  (with-meta
    [::map-desc lifecycle f]
    {`create (fn [_ desc opts]
               (create lifecycle (f desc) opts))
     `advance (fn [_ component desc opts]
                (advance lifecycle component (f desc) opts))
     `delete (fn [_ component opts]
               (delete lifecycle component opts))}))