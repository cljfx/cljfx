(ns cljfx.lifecycle
  "Part of a public API

  All Lifecycle implementations should be treated as Lifecycle protocol implementations
  only, their internals are subject to change

  All Component implementations created by lifecycles should be treated as Component
  protocol implementations only, their internals are subject to change"
  (:require [cljfx.component :as component]
            [cljfx.coerce :as coerce]
            [cljfx.prop :as prop]
            [cljfx.context :as context]
            [clojure.set :as set]))

(set! *warn-on-reflection* true)

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc opts] "Creates component")
  (advance [this component desc opts] "Advances component")
  (delete [this component opts] "Deletes component"))

(defn annotate [lifecycle id]
  (vary-meta lifecycle assoc :cljfx/id id))

(defn- desc->lifecycle [desc opts]
  (let [type (:fx/type desc)
        type->lifecycle (:fx.opt/type->lifecycle opts)]
    (or (type->lifecycle type)
        type)))

(defn- create-dynamic-component [lifecycle child-desc opts]
  (with-meta
    {:lifecycle lifecycle
     :child (create lifecycle child-desc opts)}
    {`component/instance #(-> % :child component/instance)}))

(def dynamic
  (reify Lifecycle
    (create [_ desc opts]
      (let [lifecycle (desc->lifecycle desc opts)]
        (create-dynamic-component lifecycle desc opts)))
    (advance [_ component desc opts]
      (let [lifecycle (:lifecycle component)
            new-lifecycle (desc->lifecycle desc opts)]
        (if (identical? lifecycle new-lifecycle)
          (-> component
              (update :child #(advance lifecycle % desc opts)))
          (do (delete lifecycle (:child component) opts)
              (create-dynamic-component new-lifecycle desc opts)))))
    (delete [_ component opts]
      (delete (:lifecycle component) (:child component) opts))))

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

(def change-listener
  (wrap-coerce event-handler coerce/change-listener))

(def list-change-listener
  (wrap-coerce event-handler coerce/list-change-listener))

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
                         (= opts (:opts component)))
                  component
                  (create this desc opts)))
     `delete (fn [_ _ _])}))

(defn advance-prop-map [props props-desc props-config instance opts]
  (reduce
    (fn [acc k]
      (let [old-e (find props k)
            new-e (find props-desc k)]
        (cond
          (and (some? old-e) (some? new-e))
          (let [component (val old-e)
                desc (val new-e)
                prop-config (get props-config k)
                new-component (advance (prop/lifecycle prop-config) component desc opts)]
            (prop/replace! prop-config instance component new-component)
            (assoc acc k new-component))

          (some? old-e)
          (let [prop-config (get props-config k)]
            (prop/retract! prop-config instance (val old-e))
            (delete (prop/lifecycle prop-config) (val old-e) opts)
            (dissoc acc k))

          :else
          (let [prop-config (get props-config k)
                component (create (prop/lifecycle prop-config) (val new-e) opts)]
            (prop/assign! prop-config instance component)
            (assoc acc k component)))))
    props
    (set/union (set (keys props)) (set (keys props-desc)))))

(defn detached-prop-map [props-config]
  (with-meta
    [::detached-prop-map props-config]
    {`create (fn [_ desc opts]
               (with-meta
                 {:desc desc
                  :opts opts
                  :value (if (fn? desc)
                           (fn [props instance item empty]
                             (let [props-desc (if empty {} (desc item))]
                               (advance-prop-map props props-desc props-config instance opts)))
                           desc)}
                 {`component/instance :value}))
     `advance (fn [this component desc opts]
                (if (and (= desc (:desc component))
                         (= opts (:opts component)))
                  component
                  (create this desc opts)))
     `delete (fn [_ _ _])}))

(defn- ordered-keys+key->val
  "Return a vec of ordered calculated keys and a map of calculated keys to components

  Example:
  ```
  (ordered-keys+key->val #(-> % meta (get :key ::no-key))
                         [{:x 1}
                          (with-meta {:key 1} {:key 1})
                          (with-meta {:also 1} {:key 1})
                          {}])
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
      [::extra-props lifecycle props-config]
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
             (update with-child :props advance-prop-map props-desc props-config instance opts)
             (do
               ;; TODO this is wrong, should re-create props
               (doseq [[k v] props-desc]
                 (prop/assign! (get props-config k) new-instance v))
               (assoc with-child :props props-desc)))))

       `delete
       (fn [_ component opts]
         (doseq [[k v] (:props component)]
           (delete (prop/lifecycle (get props-config k)) v opts))
         (delete lifecycle (:child component) opts))})))

(defn- props-on [props-config instance]
  (reify Lifecycle
    (create [_ desc opts]
      (reduce-kv
        (fn [acc k v]
          (let [prop (get props-config k)
                prop-value (create (prop/lifecycle prop) v opts)]
            (prop/assign! prop instance prop-value)
            (assoc acc k prop-value)))
        desc
        desc))
    (advance [_ component desc opts]
      (advance-prop-map component desc props-config instance opts))
    (delete [_ component opts]
      (doseq [[k v] component]
        (delete (prop/lifecycle (get props-config k)) v opts)))))

(defn make-ext-with-props [lifecycle props-config]
  (annotate (reify Lifecycle
              (create [_ {child-desc :desc props-desc :props} opts]
                (let [child (create lifecycle child-desc opts)
                      instance (component/instance child)
                      props-lifecycle (props-on props-config instance)]
                  (with-meta {:child child
                              :props-lifecycle props-lifecycle
                              :props (create props-lifecycle props-desc opts)}
                             {`component/instance #(-> % :child component/instance)})))
              (advance [_ component {child-desc :desc props-desc :props} opts]
                (let [{:keys [child props props-lifecycle]} component
                      old-instance (component/instance child)
                      new-child (advance lifecycle child child-desc opts)
                      new-instance (component/instance new-child)
                      with-child (assoc component :child new-child)]
                  (if (identical? old-instance new-instance)
                    (assoc with-child :props (advance props-lifecycle props props-desc opts))
                    (do
                      (delete props-lifecycle props opts)
                      (let [new-props-lifecycle (props-on props-config new-instance)]
                        (assoc with-child :props-lifecycle new-props-lifecycle
                                          :props (create new-props-lifecycle props-desc opts)))))))
              (delete [_ {:keys [child props props-lifecycle]} opts]
                (delete props-lifecycle props opts)
                (delete lifecycle child opts)))
            'cljfx.api/make-ext-with-props))

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

(defn wrap-map-desc [lifecycle f & args]
  (with-meta
    [::map-desc lifecycle f]
    {`create (fn [_ desc opts]
               (create lifecycle (apply f desc args) opts))
     `advance (fn [_ component desc opts]
                (advance lifecycle component (apply f desc args) opts))
     `delete (fn [_ component opts]
               (delete lifecycle component opts))}))

(defn wrap-context-desc [lifecycle]
  (with-meta
    [::context-desc lifecycle]
    {`create (fn [_ desc opts]
               (with-meta
                 {:context desc
                  :child (create lifecycle desc (assoc opts :fx/context desc))}
                 {`component/instance #(-> % :child component/instance)}))
     `advance (fn [_ component desc opts]
                (-> component
                    (assoc :context desc)
                    (update :child #(advance lifecycle % desc (assoc opts :fx/context desc)))))
     `delete (fn [_ component opts]
               (context/clear-cache! (:context component))
               (delete lifecycle (:child component) opts))}))

(defn- call-context-fn [context desc]
  ((:fx/type desc) (-> desc (dissoc :fx/type) (assoc :fx/context context))))

(defn- sub-context-fn [desc opts]
  (let [context (:fx/context opts)]
    (context/sub-ctx context call-context-fn desc)))

(def context-fn->dynamic
  (with-meta
    [::context-fn->dynamic]
    {`create (fn [_ desc opts]
               (create dynamic (sub-context-fn desc opts) opts))
     `advance (fn [_ component desc opts]
                (advance dynamic component (sub-context-fn desc opts) opts))
     `delete (fn [_ component opts]
               (delete dynamic component opts))}))

(defn wrap-on-instance-lifecycle [lifecycle]
  (with-meta
    [::on-instance-lifecycle lifecycle]
    {`create (fn [_ {:keys [on-created on-deleted desc]} opts]
               (let [child (create lifecycle desc opts)]
                 (when on-created
                   (on-created (component/instance child)))
                 (with-meta
                   {:on-deleted on-deleted
                    :child child}
                   {`component/instance #(-> % :child component/instance)})))
     `advance (fn [_ component {:keys [on-advanced on-deleted desc]} opts]
                (let [old-child (:child component)
                      old-instance (component/instance old-child)
                      new-child (advance lifecycle old-child desc opts)
                      new-instance (component/instance new-child)]
                  (when (and on-advanced (not= old-instance new-instance))
                    (on-advanced old-instance new-instance))
                  (assoc component :child new-child :on-deleted on-deleted)))
     `delete (fn [_ {:keys [child on-deleted]} opts]
               (delete lifecycle child opts)
               (when on-deleted
                 (on-deleted (component/instance child))))}))

(def instance-factory
  (with-meta
    [::instance-factory]
    {`create (fn [_ {:keys [create]} _]
               (with-meta
                 {:create create
                  :instance (create)}
                 {`component/instance :instance}))
     `advance (fn [_ component {:keys [create]} _]
                (if (identical? create (:create component))
                  component
                  (assoc component :create create :instance (create))))
     `delete (fn [_ _ _])}))

(defn wrap-let-refs [lifecycle]
  (reify Lifecycle
    (create [_ {:keys [desc refs]} opts]
      (let [ref-components (reduce (fn [acc [k v]]
                                     (assoc acc k (create lifecycle v opts)))
                                   refs
                                   refs)]
        (with-meta
          {:refs ref-components
           :child (create lifecycle desc (update opts ::refs merge ref-components))}
          {`component/instance #(-> % :child component/instance)})))
    (advance [_ component {:keys [desc refs]} opts]
      (let [old-refs (:refs component)
            refs (reduce
                   (fn [acc k]
                     (let [old-e (find old-refs k)
                           new-e (find refs k)]
                       (cond
                         (and (some? old-e) (some? new-e))
                         (let [component (val old-e)
                               desc (val new-e)]
                           (assoc acc k (advance lifecycle component desc opts)))

                         (some? old-e)
                         (do (delete lifecycle (val old-e) opts)
                             (dissoc acc k))

                         :else
                         (assoc acc k (create lifecycle (val new-e) opts)))))
                   old-refs
                   (set/union (set (keys old-refs))
                              (set (keys refs))))]
        (-> component
            (assoc :refs refs)
            (update :child #(advance lifecycle % desc (update opts ::refs merge refs))))))
    (delete [_ {:keys [child refs]} opts]
      (doseq [ref-component (vals refs)]
        (delete lifecycle ref-component opts))
      (delete lifecycle child opts))))

(defn get-ref [desc->ref]
  (reify Lifecycle
    (create [_ desc opts]
      (get-in opts [::refs (desc->ref desc)]))
    (advance [_ _ desc opts]
      (get-in opts [::refs (desc->ref desc)]))
    (delete [_ _ _])))

(defn- merge-env [env m]
  (reduce-kv assoc env m))

(defn wrap-set-env [lifecycle]
  (reify Lifecycle
    (create [_ {:keys [env desc]} opts]
      (create lifecycle desc (update opts ::env merge-env env)))
    (advance [_ component {:keys [env desc]} opts]
      (advance lifecycle component desc (update opts ::env merge-env env)))
    (delete [_ component opts]
      (delete lifecycle component opts))))

(defn- put-env [desc env keys]
  (if (map? keys)
    (reduce-kv #(assoc %1 %3 (get env %2)) desc keys)
    (reduce #(assoc %1 %2 (get env %2)) desc keys)))

(defn wrap-get-env [lifecycle]
  (reify Lifecycle
    (create [_ {:keys [env desc]} opts]
      (create lifecycle (put-env desc (::env opts) env) opts))
    (advance [_ component {:keys [env desc]} opts]
      (advance lifecycle component (put-env desc (::env opts) env) opts))
    (delete [_ component opts]
      (delete lifecycle component opts))))

(defn map-of [lifecycle]
  (reify Lifecycle
    (create [_ desc opts]
      (let [comps (reduce-kv #(assoc %1 %2 (create lifecycle %3 opts)) desc desc)]
        (with-meta
          {:components comps
           :instance (reduce-kv #(assoc %1 %2 (component/instance %3)) comps comps)}
          {`component/instance :instance})))
    (advance [_ component desc opts]
      (loop [components (:components component)
             instance (:instance component)
             ks (set/union (set (keys desc))
                           (set (keys components)))]
        (if (empty? ks)
          (assoc component :components components :instance instance)
          (let [[k & rest-ks] ks
                old-e (find components k)
                new-e (find desc k)]
            (cond
              (and (some? old-e) (some? new-e))
              (let [c (advance lifecycle (val old-e) (val new-e) opts)]
                (recur (assoc components k c)
                       (assoc instance k (component/instance c))
                       rest-ks))

              (some? old-e)
              (do (delete lifecycle (val old-e) opts)
                  (recur (dissoc components k)
                         (dissoc instance k)
                         rest-ks))

              :else
              (let [c (create lifecycle (val new-e) opts)]
                (recur (assoc components k c)
                       (assoc instance k (component/instance c))
                       rest-ks)))))))
    (delete [_ component opts]
      (doseq [v (vals (:components component))]
        (delete lifecycle v opts)))))

(defn- create-if-desc-component [lifecycle child-desc opts]
  (with-meta
    {:lifecycle lifecycle
     :child (create lifecycle child-desc opts)}
    {`component/instance #(-> % :child component/instance)}))

(defn if-desc [pred then-lifecycle else-lifecycle]
  (reify Lifecycle
    (create [_ desc opts]
      (let [lifecycle (if (pred desc) then-lifecycle else-lifecycle)]
        (create-if-desc-component lifecycle desc opts)))
    (advance [_ component desc opts]
      (let [old-lifecycle (:lifecycle component)
            new-lifecycle (if (pred desc) then-lifecycle else-lifecycle)]
        (if (identical? old-lifecycle new-lifecycle)
          (update component :child #(advance old-lifecycle % desc opts))
          (do (delete old-lifecycle (:child component) opts)
              (create-if-desc-component new-lifecycle desc opts)))))
    (delete [_ component opts]
      (delete (:lifecycle component) (:child component) opts))))
