(ns cljfx.event-handler
  "Part of a public API"
  (:import [clojure.lang Agent]))

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
     (doseq [[fx-effect value] (f event)
             :let [consumer (effect-id->consumer fx-effect)]]
       (if consumer
         (consumer value dispatch!)
         (throw (ex-info (str "Effect " fx-effect " does not exist")
                         {:effect fx-effect
                          :existing-effects (keys effect-id->consumer)})))))))

(defn- process-event [_ f e dispatch-async! *maybe-promise]
  (f e dispatch-async!)
  (when *maybe-promise
    (deliver *maybe-promise nil)))

(defn wrap-async [f agent-options]
  (let [*agent (apply agent nil (mapcat identity agent-options))
        executor (:fx/executor agent-options Agent/pooledExecutor)]
    (fn dispatch-async! [event]
      (if (:fx/sync event)
        (let [*promise (promise)]
          (send-via executor *agent process-event f event dispatch-async! *promise)
          @*promise)
        (send-via executor *agent process-event f event dispatch-async! nil))
      nil)))
