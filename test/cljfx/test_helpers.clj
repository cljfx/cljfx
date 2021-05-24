(ns cljfx.test-helpers
  (:require [cljfx.context :as context]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.component :as component]
            [cljfx.api :as fx]))

(defn mk-state [init]
  (let [state (atom (assoc init :history []))
        grab-history (fn []
                       (let [[{:keys [history]}]
                             (swap-vals! state assoc :history [])]
                         history))]
    {:state state
     :grab-history grab-history}))

(defn mk-props
  ([pks] (mk-props pks (mk-state {})))
  ([pks {:keys [state] :as state-m}]
   (let [mk-prop (fn [pkw]
                   (prop/make
                     (reify mutator/Mutator
                       (assign! [_ instance coerce value]
                         (swap! state
                                #(-> %
                                     (update :history conj
                                             {:op :assign!
                                              :prop pkw
                                              :instance instance
                                              :coerced-value (coerce value)
                                              :value value}))))
                       (replace! [_ instance coerce old-value new-value]
                         (swap! state
                                #(-> %
                                     (update :history conj
                                             {:op :replace!
                                              :prop pkw
                                              :instance instance
                                              :coerced-new-value (coerce new-value)
                                              :old-value old-value
                                              :new-value new-value}))))
                       (retract! [_ instance coerce value]
                         (swap! state
                                #(-> %
                                     (update :history conj
                                             {:op :retract!
                                              :prop pkw
                                              :instance instance
                                              :coerced-value (coerce value)
                                              :value value})))))
                     lifecycle/scalar
                     :coerce pr-str))
         props-config (into {}
                            (map (juxt identity mk-prop))
                            pks)]
     (assoc state-m :props-config props-config))))

(defn mk-logging-lifecycle [{:keys [state] :as state-m}]
  (let [logging-lifecycle
        (reify
          lifecycle/Lifecycle
          (lifecycle/create [_ desc opts]
            (swap! state #(-> %
                              (update :history conj
                                      {:op :create
                                       :desc desc
                                       :opts opts})))
            :create)
          (lifecycle/advance [_ component desc opts]
            (let [[{:keys [next-advance-instance]}]
                  (swap-vals! state #(-> %
                                         (dissoc :next-advance-instance)
                                         (update :history conj
                                                 {:op :advance
                                                  :component component
                                                  :desc desc
                                                  :opts opts})))]
              (or next-advance-instance :advance)))
          (lifecycle/delete [_ component opts]
            (swap! state #(-> %
                              (update :history conj
                                      {:op :delete
                                       :component component
                                       :opts opts})))
            :delete))]
    (assoc state-m :logging-lifecycle logging-lifecycle)))

(defn sort-by-from [cmp n c]
  (let [[l r] (split-at n c)]
    (concat l (sort-by cmp r))))
