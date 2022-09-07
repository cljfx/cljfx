(ns cljfx.ext.cell-factory
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.component :as component]
            [cljfx.coerce :as coerce]
            [cljfx.prop :as prop]
            [cljfx.fx :as fx])
  (:import [javafx.util Callback]
           [javafx.scene.control Cell IndexedCell]
           [javafx.beans InvalidationListener]
           [javafx.beans.value ObservableValue]))

(defn- noop [_] {})

(defn- advance-slot [{:keys [cell props-desc] :as slot} {:keys [props-config opts]}]
  (update slot :props lifecycle/advance-prop-map props-desc props-config cell opts))

(defn- describe-slot [{:keys [item empty cell] :as slot} {:keys [describe] :as state}]
  (-> slot
      (assoc :props-desc (if (or empty (and (instance? IndexedCell cell)
                                            (neg? (.getIndex ^IndexedCell cell))))
                           {}
                           (describe item)))
      (advance-slot state)))

(defn- set-key-and-advance-slot [state index k v]
  (update-in state [:slots index] #(if (identical? v (get % k))
                                     %
                                     (-> % (assoc k v) (describe-slot state)))))

(defn- advance-state [state describe opts]
  (let [slot-fn (if (not= describe (:describe state)) describe-slot advance-slot)
        new-state (assoc state :describe describe :opts opts)]
    (update new-state :slots
            (fn [slots]
              (mapv #(slot-fn % new-state) slots)))))

(def lifecycle
  (reify lifecycle/Lifecycle
    (create [_ {:keys [fx/cell-type describe] :or {describe noop}} opts]
      (let [composite (cond
                        (keyword? cell-type)
                        (fx/keyword->lifecycle cell-type)

                        (and (map? cell-type) (:ctor cell-type) (:props cell-type))
                        cell-type

                        :else
                        (coerce/fail :fx/cell-type cell-type))
            *state (volatile! {:describe describe
                               :props-config (:props composite)
                               :opts opts
                               :slots []})
            on-item-changed-fn (bound-fn [index ov]
                                 (vswap! *state set-key-and-advance-slot index :item (.getValue ^ObservableValue ov)))
            on-empty-changed-fn (bound-fn [index ov]
                                  (vswap! *state set-key-and-advance-slot index :empty (.getValue ^ObservableValue ov)))
            cb (reify Callback
                 (call [_ _]
                   (let [state @*state
                         ^Cell ret ((:ctor composite))]
                     (if (:deleted state)
                       ret
                       (let [index (count (:slots @*state))
                             on-item-changed
                             (reify InvalidationListener
                               (invalidated [_ ov]
                                 (on-item-changed-fn index ov)))
                             on-empty-changed
                             (reify InvalidationListener
                               (invalidated [_ ov]
                                 (on-empty-changed-fn index ov)))]
                         (vswap! *state update :slots conj {:cell ret
                                                            :item (.getItem ret)
                                                            :empty (.isEmpty ret)
                                                            :props {}
                                                            :props-desc {}
                                                            :on-item-changed on-item-changed
                                                            :on-empty-changed on-empty-changed})
                         (.addListener (.itemProperty ret) on-item-changed)
                         (.addListener (.emptyProperty ret) on-empty-changed)
                         ret)))))]
        (with-meta {:fx/cell-type cell-type
                    :instance cb
                    :*state *state}
                   {`component/instance :instance})))
    (advance [this component {:keys [fx/cell-type describe] :or {describe noop} :as desc} opts]
      (if (not= cell-type (:fx/cell-type component))
        (do (lifecycle/delete this component opts)
            (lifecycle/create this desc opts))
        (doto component (-> :*state (vswap! advance-state describe opts)))))
    (delete [_ {:keys [*state]} opts]
      (vswap! *state assoc :deleted true)
      (let [{:keys [slots props-config]} @*state]
        (doseq [slot slots
                :let [{:keys [props
                              ^InvalidationListener on-item-changed
                              ^InvalidationListener on-empty-changed
                              ^Cell cell]} slot
                      _ (.removeListener (.itemProperty cell) on-item-changed)
                      _ (.removeListener (.emptyProperty cell) on-empty-changed)]
                [k v] props]
          (lifecycle/delete (prop/lifecycle (get props-config k)) v opts))))))
