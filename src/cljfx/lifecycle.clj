(ns cljfx.lifecycle
  "Part of a public API

  All Lifecycle implementations should be treated as Lifecycle protocol implementations
  only, their internals are subject to change

  All Component implementations created by lifecycles should be treated as Component
  protocol implementations only, their internals are subject to change"
  (:require [cljfx.component :as component]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.platform :as platform]
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

(defn binding-prop [bind lifecycle]
  (prop/->Prop
    nil
    (mutator/adder-remover
      (fn create-binding [instance f] (f instance))
      (fn delete-binding [_ f] (f)))
    (wrap-coerce
      lifecycle
      (fn coerce-binding [prop-instance]
        (let [state (volatile! false)]
          (fn binding
            ([]
             (let [dispose @state]
               (when-not dispose
                 (throw (IllegalStateException. "Cannot dispose a binding that is not initialized")))
               (vreset! state false)
               (when (fn? dispose) (dispose))))
            ([instance]
             (when @state (throw (IllegalStateException. "Cannot initialize a binding that is already initialized")))
             (let [dispose (bind instance prop-instance)]
               (vreset! state (if (fn? dispose) dispose true))))))))
    identity))

(def scalar
  (with-meta
    [::scalar]
    {`create (fn [_ v _] v)
     `advance (fn [_ _ v _] v)
     `delete (fn [_ _ _])}))

(defn- create-event-handler-component [desc opts]
  (cond
    (map? desc)
    (let [map-event-handler (:fx.opt/map-event-handler opts)
          v (volatile! desc)]
      (with-meta
        {:kind :map
         :volatile v
         :fx.opt/map-event-handler map-event-handler
         :value #(when-not *in-progress?* (map-event-handler (assoc @v :fx/event %)))}
        {`component/instance :value}))

    (fn? desc)
    (let [v (volatile! desc)]
      (with-meta
        {:kind :fn
         :volatile v
         :value #(when-not *in-progress?* (@v %))}
        {`component/instance :value}))

    :else
    (with-meta
      {:kind :else
       :value desc}
      {`component/instance :value})))

(def event-handler
  (with-meta
    [::event-handler]
    {`create (fn [_ desc opts]
               (create-event-handler-component desc opts))
     `advance (fn [_ component desc opts]
                (let [component-kind (:kind component)
                      desc-kind (if (map? desc) :map (if (fn? desc) :fn :else))]
                  (if (not (identical? desc-kind component-kind))
                    (create-event-handler-component desc opts)
                    (case component-kind
                      :map
                      (if (= (:fx.opt/map-event-handler component)
                             (:fx.opt/map-event-handler opts))
                        (do (vreset! (:volatile component) desc) component)
                        (create-event-handler-component desc opts))
                      :fn
                      (do (vreset! (:volatile component) desc) component)
                      :else
                      (if (= desc (:value component))
                        component
                        (create-event-handler-component desc opts))))))
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
                prop (prop/from props-config k)
                new-component (advance (prop/lifecycle prop) component desc opts)]
            (prop/replace! prop instance component new-component)
            (assoc acc k new-component))

          (some? old-e)
          (let [prop (prop/from props-config k)]
            (prop/retract! prop instance (val old-e))
            (delete (prop/lifecycle prop) (val old-e) opts)
            (dissoc acc k))

          :else
          (let [prop (prop/from props-config k)
                component (create (prop/lifecycle prop) (val new-e) opts)]
            (prop/assign! prop instance component)
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
                         (assoc acc k (create (prop/lifecycle (prop/from props-config k))
                                              (get prop-desc k)
                                              opts)))
                       prop-desc
                       (keys prop-desc))]
           (doseq [[k v] props]
             (prop/assign! (prop/from props-config k) instance v))
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
                 (prop/assign! (prop/from props-config k) new-instance v))
               (assoc with-child :props props-desc)))))

       `delete
       (fn [_ component opts]
         (doseq [[k v] (:props component)]
           (delete (prop/lifecycle (prop/from props-config k)) v opts))
         (delete lifecycle (:child component) opts))})))

(defn- props-on [props-config instance]
  (reify Lifecycle
    (create [_ desc opts]
      (reduce-kv
        (fn [acc k v]
          (let [prop (prop/from props-config k)
                prop-value (create (prop/lifecycle prop) v opts)]
            (prop/assign! prop instance prop-value)
            (assoc acc k prop-value)))
        desc
        desc))
    (advance [_ component desc opts]
      (advance-prop-map component desc props-config instance opts))
    (delete [_ component opts]
      (doseq [[k v] component]
        (delete (prop/lifecycle (prop/from props-config k)) v opts)))))

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



(defn- complete-rendering [old-state value new-component]
  (-> old-state
      (assoc :component new-component)
      (cond-> (= value (:value old-state))
        (dissoc :value))))

(defn- complete-advance [old-state desc key opts value new-component]
  (-> old-state
      (assoc :desc desc :key key :opts opts)
      (complete-rendering value new-component)))

(defn- perform-render [state]
  ;; advance is a mutating operation on DOM, can't retry => no swap!
  ;; new request may arrive during advancing => we might enqueue another request
  (let [current-state @state
        ;; default to ::deleted so that advance can signal that there is no need
        ;; to re-render by dissoc-ing the :value
        value (:value current-state ::deleted)]
    (when-not (identical? value ::deleted)
      (let [{:keys [desc key opts component]} current-state
            old-instance (component/instance component)
            new-component (advance root component (assoc desc key value) opts)
            new-instance (component/instance new-component)
            _ (when-not (= old-instance new-instance)
                (throw (ex-info "Instance replace forbidden"
                                {:old old-instance :new new-instance})))
            new-state (swap! state complete-rendering value new-component)]
        (when (contains? new-state :value)
          (platform/run-later (perform-render state)))))))

(defn- set-new-value-if-not-deleted [state value]
  (if (identical? ::deleted (:value state))
    state
    (assoc state :value value)))

(defn- request-render [state value]
  (let [[old new] (swap-vals! state set-new-value-if-not-deleted value)]
    (when (and (not (contains? old :value))
               (contains? new :value))
      (platform/run-later (perform-render state)))))

(def ext-watcher
  (annotate
    (reify Lifecycle
      (create [_ {:keys [ref desc key] :or {key :value}} opts]
        (let [state (atom {:component (create dynamic (assoc desc key @ref) opts)
                           :ref ref
                           :desc desc
                           :key key
                           :opts opts}
                          :meta {`component/instance #(component/instance (:component @%))})]
          (add-watch ref state #(request-render state %4))
          state))
      (advance [this component {:keys [ref desc key] :or {key :value} :as this-desc} opts]
        (let [current-state @component
              current-ref (:ref current-state)]
          (if (= ref current-ref)
            (let [value @ref
                  old-component (:component @component)
                  old-instance (component/instance old-component)
                  new-component (advance dynamic old-component (assoc desc key value) opts)
                  new-instance (component/instance new-component)]
              ;; we report error here because new instance won't be picked up, since
              ;; instance for old component and new component will stay the same, because
              ;; the component is the same atom
              (when-not (= old-instance new-instance)
                (throw (ex-info "Instance replace forbidden" {:old old-instance :new new-instance})))
              (doto component (swap! complete-advance desc key opts value new-component)))
            (do (delete this component opts)
                (create this this-desc opts)))))
      (delete [_ component opts]
        (let [current-state @component]
          (remove-watch (:ref current-state) component)
          (swap! component assoc :value ::deleted)
          (delete dynamic (:component current-state) opts))))
    'cljfx.api/ext-watcher))

(def ^:private ext-convey-local-state
  (reify Lifecycle
    (create [_ {:keys [desc value swap-state key swap-key]} opts]
      (create dynamic (assoc desc key value swap-key swap-state) opts))
    (advance [_ component {:keys [desc value swap-state key swap-key]} opts]
      (advance dynamic component (assoc desc key value swap-key swap-state) opts))
    (delete [_ component opts]
      (delete dynamic component opts))))

(defn- reset-local-state [local-state-component new-initial-state reset-fn]
  (swap! (:ref local-state-component) reset-fn new-initial-state)
  (assoc local-state-component :initial-state new-initial-state))

(defn- default-reset-local-state-fn [_old new] new)

(def ext-state
  (annotate
    (reify Lifecycle
      (create [_ {:keys [initial-state desc] :as this-desc} opts]
        (let [a (atom initial-state)
              swap-state (partial swap! a)]
          (with-meta
            {:ref a
             :swap-state swap-state
             :initial-state initial-state
             :child (create ext-watcher
                            {:ref a
                             :desc {:fx/type ext-convey-local-state
                                    :desc desc
                                    :swap-state swap-state
                                    :key (:key this-desc :state)
                                    :swap-key (:swap-key this-desc :swap-state)}}
                            opts)}
            {`component/instance #(-> % :child component/instance)})))
      (advance [_ component {:keys [initial-state desc] :as this-desc} opts]
        (-> component
            (cond-> (not= initial-state (:initial-state component))
                    (reset-local-state initial-state (:reset this-desc default-reset-local-state-fn)))
            (update :child #(advance
                              ext-watcher
                              %
                              {:ref (:ref component)
                               :desc {:fx/type ext-convey-local-state
                                      :desc desc
                                      :swap-state (:swap-state component)
                                      :key (:key this-desc :state)
                                      :swap-key (:swap-key this-desc :swap-state)}} opts))))
      (delete [_ component opts]
        (delete ext-watcher (:child component) opts)))
    'cljfx.api/ext-state))

(def ext-effect
  (annotate
    (reify Lifecycle
      (create [_ {:keys [args fn desc]} opts]
        (let [ret (apply fn args)]
          (with-meta
            {:args args
             :fn fn
             :stop (when (fn? ret) ret)
             :child (create dynamic desc opts)}
            {`component/instance #(-> % :child component/instance)})))
      (advance [_ component {:keys [args fn desc]} opts]
        (let [new-component (update component :child #(advance dynamic % desc opts))]
          (if (and (= args (:args component))
                   (= fn (:fn component)))
            new-component
            (let [stop (:stop component)
                  _ (when stop (stop))
                  ret (apply fn args)]
              (assoc new-component :args args :fn fn :stop (when (fn? ret) ret))))))
      (delete [_ {:keys [stop child]} opts]
        (when stop (stop))
        (delete dynamic child opts)))
    'cljfx.api/ext-effect))

(def ext-recreate-on-key-changed
  (annotate
    (reify Lifecycle
      (create [_ {:keys [key desc]} opts]
        (with-meta
          {:key key
           :child (create dynamic desc opts)}
          {`component/instance #(-> % :child component/instance)}))
      (advance [this component {:keys [key desc] :as this-desc} opts]
        (if (= key (:key component))
          (update component :child #(advance dynamic % desc opts))
          (do (delete this component opts)
              (create this this-desc opts))))
      (delete [_ component opts]
        (delete dynamic (:child component) opts)))
    'cljfx.api/ext-recreate-on-key-changed))