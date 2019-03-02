(ns cljfx.event-handler
  "Part of a public API")

(defn make-deref-co-effect [*ref]
  #(deref *ref))

(defn wrap-co-effects [f co-effect-id->producer]
  (fn [event]
    (f (reduce (fn [acc [k v]]
                 (assoc acc k (v)))
               event
               co-effect-id->producer))))

(defn make-reset-effect [*atom]
  (fn [v _]
    (reset! *atom v)))

(defn dispatch-effect [v dispatch!]
  (dispatch! v))

(defn wrap-effects [f effect-id->consumer]
  (fn dispatch-sync!
    ([event]
     (dispatch-sync! event dispatch-sync!))
    ([event dispatch!]
     (doseq [[fx-effect value] (f event)]
       ((effect-id->consumer fx-effect) value dispatch!)))))

(defn- process-event [_ f e dispatch-async! *maybe-promise]
  (f e dispatch-async!)
  (when *maybe-promise
    (deliver *maybe-promise nil)))

(defn wrap-async [f agent-options]
  (let [*agent (apply agent nil (mapcat identity agent-options))]
    (fn dispatch-async! [event]
      (if (:fx/sync event)
        (let [*promise (promise)]
          (send *agent process-event f event dispatch-async! *promise)
          @*promise)
        (send *agent process-event f event dispatch-async! nil))
      nil)))
