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
            [clojure.set :as set])
  (:import [clojure.lang IHashEq IPersistentVector MapEntry Murmur3 RT Util]
           [java.util HashMap IdentityHashMap Iterator]))

(set! *warn-on-reflection* true)

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc opts] "Creates component")
  (advance [this component desc opts] "Advances component")
  (delete [this component opts] "Deletes component"))

(defn annotate [lifecycle id]
  (vary-meta lifecycle assoc :cljfx/id id))

(def scalar
  (reify Lifecycle
    (create [_ v _] v)
    (advance [_ _ v _] v)
    (delete [_ _ _])))

(defn- desc->lifecycle [desc opts]
  (if (nil? desc)
    scalar
    (let [type (:fx/type desc)
          type->lifecycle (:fx.opt/type->lifecycle opts)]
      (or (type->lifecycle type)
          type))))

(deftype DynamicComponent [lifecycle child]
  component/Component
  (instance [_] (component/instance child)))

(defn- create-dynamic-component [lifecycle child-desc opts]
  (->DynamicComponent lifecycle (create lifecycle child-desc opts)))

(def dynamic
  (reify Lifecycle
    (create [_ desc opts]
      (let [lifecycle (desc->lifecycle desc opts)]
        (create-dynamic-component lifecycle desc opts)))
    (advance [_ component desc opts]
      (let [^DynamicComponent component component
            old-lifecycle (.-lifecycle component)
            old-child (.-child component)
            new-lifecycle (desc->lifecycle desc opts)]
        (if (identical? old-lifecycle new-lifecycle)
          (let [new-child (advance new-lifecycle old-child desc opts)]
            (if (identical? old-child new-child)
              component
              (->DynamicComponent new-lifecycle new-child)))
          (do (delete old-lifecycle (.-child component) opts)
              (create-dynamic-component new-lifecycle desc opts)))))
    (delete [_ component opts]
      (let [^DynamicComponent component component]
        (delete (.-lifecycle component) (.-child component) opts)))))

(def ^:dynamic *in-progress?* false)

(def ^:dynamic *request-render* nil)

(defn- create-root [lifecycle desc opts]
  (let [state
        (atom {:desc desc
               :opts opts
               :revision 0
               :rendered-revision 0
               :queued false
               :deleted false})]
    (letfn [(enqueue-render []
              (platform/run-later (perform-render)))
            (request-render []
              (let [[old new]
                    (swap-vals! state
                                (fn request-root-render [s]
                                  (if (:deleted s)
                                    s
                                    (-> s
                                        (update :revision inc)
                                        (assoc :queued true)))))]
                (when (and (not (:queued old)) (:queued new))
                  (enqueue-render))))
            (perform-render []
              (let [{:keys [component desc opts revision rendered-revision deleted]}
                    @state]
                (when-not deleted
                  (try
                    (when (not= revision rendered-revision)
                      (let [child
                            (binding [*in-progress?* true
                                      *request-render* request-render]
                              (advance lifecycle component desc opts))]
                        (vreset! (:instance @state) (component/instance child))
                        (swap! state assoc :component child)))
                    (catch Throwable e
                      ((:fx.opt/error-handler opts) e))
                    (finally
                      (let [next-state
                            (swap! state
                                   (fn complete-root-render [s]
                                     (assoc s
                                       :rendered-revision revision
                                       :queued (and (not (:deleted s))
                                                    (not= revision (:revision s))))))]
                        (when (:queued next-state)
                          (enqueue-render))))))))]
      (swap! state assoc :request-render request-render)
      (let [child
            (binding [*in-progress?* true
                      *request-render* request-render]
              (create lifecycle desc opts))

            instance (volatile! (component/instance child))]
        (swap! state assoc :component child :instance instance)
        (with-meta {:state state :instance instance}
          {`component/instance #(deref (:instance %))})))))

(defn wrap-root [lifecycle]
  (reify Lifecycle
    (create [_ desc opts]
      (create-root lifecycle desc opts))
    (advance [_ {:keys [state] :as component} desc opts]
      (let [{child :component :keys [revision request-render]} @state
            old-instance (component/instance child)

            new-child
            (binding [*in-progress?* true
                      *request-render* request-render]
              (advance lifecycle child desc opts))

            new-instance (component/instance new-child)

            new-component
            (if (identical? old-instance new-instance)
              component
              (assoc component :instance (volatile! new-instance)))]
        (swap! state assoc
               :desc desc :opts opts :component new-child
               :instance (:instance new-component) :rendered-revision revision)
        new-component))
    (delete [_ {:keys [state]} opts]
      (let [[old _] (swap-vals! state assoc :deleted true)]
        (binding [*in-progress?* true
                  *request-render* (:request-render old)]
          (delete lifecycle (:component old) opts))))))

(def root (wrap-root dynamic))

(defn- call-dynamic-fn [desc]
  ((:fx/type desc) (dissoc desc :fx/type)))

(deftype DynamicFnComponent [child-desc desc child]
  component/Component
  (instance [_] (component/instance child)))

(defn wrap-dynamic-fn [lifecycle]
  (reify Lifecycle
    (create [_ desc opts]
      (let [child-desc (call-dynamic-fn desc)]
        (->DynamicFnComponent child-desc desc (create lifecycle child-desc opts))))
    (advance [_ component desc opts]
      (let [^DynamicFnComponent component component
            child (.-child component)]
        (if (= desc (.-desc component))
          (let [child-desc (.-child-desc component)
                new-child (advance lifecycle child child-desc opts)]
            (if (identical? child new-child)
              component
              (->DynamicFnComponent child-desc desc new-child)))
          (let [child-desc (call-dynamic-fn desc)
                new-child (advance lifecycle child child-desc opts)]
            (->DynamicFnComponent child-desc desc new-child)))))
    (delete [_ component opts]
      (delete lifecycle (.-child ^DynamicFnComponent component) opts))))

(def dynamic-fn->dynamic
  (wrap-dynamic-fn dynamic))

(deftype CoerceComponent [child value]
  component/Component
  (instance [_] value))

(defn wrap-coerce [lifecycle coerce]
  (reify Lifecycle
    (create [_ desc opts]
      (let [child (create lifecycle desc opts)]
        (->CoerceComponent child (coerce (component/instance child)))))
    (advance [_ component desc opts]
      (let [^CoerceComponent component component
            child (.-child component)
            old-instance (component/instance child)
            new-child (advance lifecycle child desc opts)
            new-instance (component/instance new-child)]
        (cond
          (not= old-instance new-instance)
          (->CoerceComponent new-child (coerce new-instance))

          (identical? child new-child)
          component

          :else
          (->CoerceComponent new-child (.-value component)))))
    (delete [_ component opts]
      (delete lifecycle (.-child ^CoerceComponent component) opts))))

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

(deftype MapEventHandlerComponent [v map-event-handler value]
  component/Component
  (instance [_] value))

(deftype FnEventHandlerComponent [v value]
  component/Component
  (instance [_] value))

(deftype ElseEventHandlerComponent [value]
  component/Component
  (instance [_] value))

(defn- create-event-handler-component [desc opts]
  (cond
    (map? desc)
    (let [map-event-handler (:fx.opt/map-event-handler opts)
          v (volatile! desc)]
      (->MapEventHandlerComponent
        v
        map-event-handler
        #(when-not *in-progress?* (map-event-handler (assoc @v :fx/event %)))))

    (fn? desc)
    (let [v (volatile! desc)]
      (->FnEventHandlerComponent
        v
        #(when-not *in-progress?* (@v %))))

    :else
    (->ElseEventHandlerComponent desc)))

(def event-handler
  (reify Lifecycle
    (create [_ desc opts]
      (create-event-handler-component desc opts))
    (advance [_ component desc opts]
      (let [component-kind (cond
                             (instance? MapEventHandlerComponent component) :map
                             (instance? FnEventHandlerComponent component) :fn
                             :else :else)
            desc-kind (if (map? desc) :map (if (fn? desc) :fn :else))]
        (if (not (identical? desc-kind component-kind))
          (create-event-handler-component desc opts)
          (case component-kind
            :map
            (let [component ^MapEventHandlerComponent component]
              (if (= (.-map-event-handler component)
                     (:fx.opt/map-event-handler opts))
                (do (vreset! (.-v component) desc) component)
                (create-event-handler-component desc opts)))

            :fn
            (let [component ^FnEventHandlerComponent component]
              (vreset! (.-v component) desc)
              component)

            :else
            (let [component ^ElseEventHandlerComponent component]
              (if (= desc (.-value component))
                component
                (create-event-handler-component desc opts)))))))
    (delete [_ _ _])))

(def callback
  (reify Lifecycle
    (create [_ desc _]
      (let [v (volatile! desc)]
        (with-meta {:volatile v :value #(when-not *in-progress?* (apply @v %&))} {`component/instance :value})))
    (advance [_ component desc _]
      (vreset! (:volatile component) desc)
      component)
    (delete [_ _ _])))

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
  (let [ret (reduce-kv
              (fn [acc k desc]
                (let [component (get props k ::no-key)]
                  (if (identical? ::no-key component)
                    (let [prop (prop/from props-config k)
                          component (create (prop/lifecycle prop) desc opts)]
                      (prop/assign! prop instance component)
                      (assoc acc k component))
                    (let [prop (prop/from props-config k)
                          new-component (advance (prop/lifecycle prop) component desc opts)]
                      (prop/replace! prop instance component new-component)
                      (assoc acc k new-component)))))
              props
              props-desc)]
    (reduce-kv
      (fn [acc k component]
        (if (contains? props-desc k)
          acc
          (let [prop (prop/from props-config k)]
            (prop/retract! prop instance component)
            (delete (prop/lifecycle prop) component opts)
            (dissoc acc k))))
      ret
      props)))

(defn- cell-props-root [props-config]
  (wrap-root
    (reify Lifecycle
      (create [_ {:keys [instance props-desc] :as desc} opts]
        (with-meta
          {:desc desc :props (advance-prop-map {} props-desc props-config instance opts)}
          {`component/instance :props}))
      (advance [_ {:keys [props] :as component}
                {:keys [instance props-desc retained-props] :as desc} opts]
        (let [props
              (if (identical? desc (:desc component))
                props
                ;; The legacy ListCell's updateItem resets text and graphic itself.
                (reduce-kv
                  (fn [acc k prop-component]
                    (if (contains? retained-props k)
                      acc
                      (do (delete (prop/lifecycle (prop/from props-config k)) prop-component opts)
                          (dissoc acc k))))
                  props
                  props))]
          (assoc component :desc desc
                 :props (advance-prop-map props props-desc props-config instance opts))))
      (delete [_ {:keys [props]} opts]
        (doseq [[k v] props]
          (delete (prop/lifecycle (prop/from props-config k)) v opts))))))

(defn detached-prop-map [props-config]
  (let [cell-root (cell-props-root props-config)]
    (reify Lifecycle
      (create [_ desc opts]
        (let [cells (IdentityHashMap.)
              disposed (volatile! false)]
          (with-meta
            {:desc desc
             :opts opts
             :cells cells
             :disposed disposed
             :value (if (fn? desc)
                      (fn update-cell [props instance item empty]
                        (if @disposed
                          {}
                          (let [cell (.get cells instance)
                                cell-desc {:instance instance
                                           :retained-props (set (keys props))
                                           :props-desc (if empty {} (desc item))}]
                            (if empty
                              (do
                                (when cell
                                  (.remove cells instance)
                                  (delete cell-root (advance cell-root cell cell-desc opts) opts))
                                {})
                              (let [new-cell (if cell
                                               (advance cell-root cell cell-desc opts)
                                               (create cell-root cell-desc opts))]
                                (.put cells instance new-cell)
                                (component/instance new-cell))))))
                      desc)}
            {`component/instance :value})))
      (advance [this component desc opts]
        (if (and (= desc (:desc component))
                 (= opts (:opts component)))
          component
          (do (delete this component opts)
              (create this desc opts))))
      (delete [_ {:keys [^IdentityHashMap cells disposed opts]} _]
        (vreset! disposed true)
        (let [components (vec (.values cells))]
          (.clear cells)
          (doseq [cell components]
            (delete cell-root cell opts)))))))

(defn- fx-key-from-map [desc]
  (:fx/key desc ::no-key))

(defn- strip-fx-key [desc]
  (dissoc desc :fx/key))

(deftype ChildKey [value ^long occurrence]
  IHashEq
  (hasheq [_]
    ;; Preserve the hash and trie traversal order of [value occurrence].
    (Murmur3/mixCollHash
     (unchecked-add-int
      (unchecked-multiply-int 31 (unchecked-add-int 31 (Util/hasheq value)))
      (Murmur3/hashLong occurrence))
     2))
  Object
  (hashCode [this]
    (.hasheq this))
  (equals [_ other]
    (and (instance? ChildKey other)
         (let [^ChildKey other other]
           (and (== occurrence (.-occurrence other))
                (Util/equiv value (.-value other)))))))

(deftype ManyComponent [instance key->component keys]
  component/Component
  (instance [_] instance))

(defn- transient-vector-prefix [^IPersistentVector v ^long end]
  (loop [ret (transient [])
         i 0]
    (if (< i end)
      (recur (conj! ret (.nth v i)) (unchecked-inc i))
      ret)))

(defn wrap-many
  ([lifecycle]
   (wrap-many lifecycle fx-key-from-map strip-fx-key))
  ([lifecycle desc->key desc->child-desc]
   (reify Lifecycle
     (create [_ desc opts]
       (let [^Iterator it (RT/iter desc)
             ^HashMap key-value->counter (HashMap.)]
         (loop [key->component (transient {})
                instance (transient [])
                keys (transient [])]
           (if (.hasNext it)
             (let [desc (.next it)
                   key-value (desc->key desc)
                   key-index (long (or (.get key-value->counter key-value) 0))
                   key (ChildKey. key-value key-index)
                   child (create lifecycle (desc->child-desc desc) opts)
                   child-instance (component/instance child)]
               (.put key-value->counter key-value (unchecked-inc key-index))
               (recur (assoc! key->component key child)
                      (cond-> instance (some? child-instance) (conj! child-instance))
                      (conj! keys key)))
             (->ManyComponent (persistent! instance)
                              (persistent! key->component)
                              (persistent! keys))))))

     (advance [_ component desc opts]
       (let [^Iterator it (RT/iter desc)
             ^ManyComponent component component
             ^IPersistentVector old-instance (.-instance component)
             old-instance-count (count old-instance)
             old-key->component (.-key->component component)
             ^IPersistentVector old-keys (.-keys component)
             old-key-count (count old-keys)
             ^HashMap key-value->counter (HashMap.)]
         (loop [index 0
                instance-index 0
                key->component nil
                instance nil
                keys nil]
           (if (.hasNext it)
             (let [desc (.next it)
                   key-value (desc->key desc)
                   key-index (long (or (.get key-value->counter key-value) 0))
                   key (ChildKey. key-value key-index)
                   old-child (get old-key->component key ::no-key)
                   new-child (if (identical? ::no-key old-child)
                               (create lifecycle (desc->child-desc desc) opts)
                               (advance lifecycle old-child (desc->child-desc desc) opts))
                   new-child-instance (component/instance new-child)]
               (.put key-value->counter key-value (unchecked-inc key-index))
               (recur
                 (unchecked-inc index)
                 (if (nil? new-child-instance)
                   instance-index
                   (unchecked-inc instance-index))
                 (if (identical? old-child new-child)
                   key->component
                   (assoc! (or key->component (transient old-key->component))
                     key
                     new-child))
                 (cond
                   (nil? new-child-instance)
                   instance

                   (some? instance)
                   (conj! instance new-child-instance)

                   (and (< instance-index old-instance-count)
                        (identical? (.nth old-instance instance-index) new-child-instance))
                   nil

                   :else
                   (conj! (transient-vector-prefix old-instance instance-index)
                          new-child-instance))
                 (cond
                   (some? keys)
                   (conj! keys key)

                   (and (< index old-key-count)
                        (= (.nth old-keys index) key))
                   nil

                   :else
                   (conj! (transient-vector-prefix old-keys index) key))))
             (let [keys (cond
                          (some? keys)
                          (persistent! keys)

                          (= index old-key-count)
                          old-keys

                          :else
                          (persistent! (transient-vector-prefix old-keys index)))
                   key->component
                   (if (identical? old-keys keys)
                     key->component
                     (let [key-set (set keys)]
                       (reduce-kv
                        (fn [ret key old-child]
                          (if (contains? key-set key)
                            ret
                            (do
                              (delete lifecycle old-child opts)
                              (dissoc! (or ret (transient old-key->component)) key))))
                        key->component
                        old-key->component)))
                   instance (cond
                              (some? instance)
                              (persistent! instance)

                              (= instance-index old-instance-count)
                              old-instance

                              :else
                              (persistent! (transient-vector-prefix old-instance instance-index)))]
               (if (and (nil? key->component)
                        (identical? old-instance instance)
                        (identical? old-keys keys))
                 component
                 (->ManyComponent
                  instance
                  (if (nil? key->component)
                    old-key->component
                    (persistent! key->component))
                  keys)))))))

     (delete [_ component opts]
       (doseq [x (vals (.-key->component ^ManyComponent component))]
         (delete lifecycle x opts))))))

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

(defn- props-on [props-config instance]
  (reify Lifecycle
    (create [_ desc opts]
      (when (some? instance)
        (reduce-kv
          (fn [acc k v]
            (let [prop (prop/from props-config k)
                  prop-value (create (prop/lifecycle prop) v opts)]
              (prop/assign! prop instance prop-value)
              (assoc acc k prop-value)))
          desc
          desc)))
    (advance [_ component desc opts]
      (when (some? instance)
        (advance-prop-map component desc props-config instance opts)))
    (delete [_ component opts]
      (doseq [[k v] component]
        (delete (prop/lifecycle (prop/from props-config k)) v opts)))))

(deftype PropsComponent [child props-lifecycle props]
  component/Component
  (instance [_] (component/instance child)))

;; A nil split forwards desc as-is and supplies an empty extra-prop map.
(defn- wrap-props [lifecycle props-config split-desc]
  (reify Lifecycle
    (create [_ desc opts]
      (let [^MapEntry split (split-desc desc)
            child (create lifecycle (if split (.key split) desc) opts)
            props-lifecycle (props-on props-config (component/instance child))]
        (->PropsComponent child props-lifecycle (create props-lifecycle (if split (.val split) {}) opts))))
    (advance [_ component desc opts]
      (let [^PropsComponent component component
            child (.-child component)
            props (.-props component)
            props-lifecycle (.-props-lifecycle component)
            old-instance (component/instance child)
            ^MapEntry split (split-desc desc)
            new-child (advance lifecycle child (if split (.key split) desc) opts)
            new-instance (component/instance new-child)
            props-desc (if split (.val split) {})]
        (if (identical? old-instance new-instance)
          (let [new-props (advance props-lifecycle props props-desc opts)]
            (if (and (identical? child new-child) (identical? props new-props))
              component
              (->PropsComponent new-child props-lifecycle new-props)))
          (do
            (delete props-lifecycle props opts)
            (let [new-props-lifecycle (props-on props-config new-instance)]
              (->PropsComponent new-child new-props-lifecycle
                                (create new-props-lifecycle props-desc opts)))))))
    (delete [_ component opts]
      (let [^PropsComponent component component]
        (delete (.-props-lifecycle component) (.-props component) opts)
        (delete lifecycle (.-child component) opts)))))

(defn make-ext-with-props [lifecycle props-config]
  (annotate (wrap-props lifecycle props-config
                        (fn split-detached-props [desc] (MapEntry/create (:desc desc) (:props desc))))
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

(defn wrap-extra-props
  "Adds configured extra props to nonnil child instances."
  [lifecycle props-config]
  (let [^IPersistentVector prop-keys (vec (keys props-config))
        key-count (.count prop-keys)]
    (wrap-props lifecycle props-config
                (fn split-extra-props [desc]
                  (loop [i 0
                         child-desc desc
                         props-desc nil]
                    (if (< i key-count)
                      (let [k (.nth prop-keys i)
                            v (get desc k ::no-key)]
                        (if (identical? ::no-key v)
                          (recur (unchecked-inc i) child-desc props-desc)
                          (recur (unchecked-inc i)
                                 (dissoc child-desc k)
                                 (assoc! (or props-desc (transient {})) k v))))
                      (when props-desc
                        (MapEntry/create child-desc (persistent! props-desc)))))))))

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

(defn- dispose-watcher [state]
  (let [[old _] (swap-vals! state assoc :deleted true)]
    (some-> (:ref old) (remove-watch state))))

(deftype WatcherComponent [state instance]
  component/Component
  (instance [_] instance))

(defn- create-watcher [ref desc key opts child]
  (let [request-render *request-render*
        state (atom {:ref ref})]
    (when ref
      (add-watch ref state
                 (fn request-watcher-render [_ _ _ _]
                   (when-not (:deleted @state)
                     (request-render)))))
    (let [value (when ref @ref)
          child-desc (assoc desc key value)

          new-child
          (if child
            (advance dynamic child child-desc opts)
            (create dynamic child-desc opts))]
      (swap! state assoc :component new-child)
      (->WatcherComponent state (component/instance new-child)))))

(def ext-watcher
  (annotate
    (reify Lifecycle
      (create [_ {:keys [ref desc key] :or {key :value}} opts]
        (create-watcher ref desc key opts nil))
      (advance [_ component {:keys [ref desc key] :or {key :value}} opts]
        (let [^WatcherComponent component component
              state (.-state component)
              current-state @state
              current-ref (:ref current-state)]
          (if (= ref current-ref)
            (let [value (when ref @ref)
                  old-component (:component current-state)
                  new-component (advance dynamic old-component (assoc desc key value) opts)
                  new-instance (component/instance new-component)]
              (swap! state assoc :component new-component)
              (if (identical? (.-instance component) new-instance)
                component
                (->WatcherComponent state new-instance)))
            (do (dispose-watcher state)
                (create-watcher ref desc key opts (:component current-state))))))
      (delete [_ component opts]
        (let [state (.-state ^WatcherComponent component)
              current-state @state]
          (dispose-watcher state)
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
