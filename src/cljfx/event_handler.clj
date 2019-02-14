(ns cljfx.event-handler
  (:require [cljfx.defaults :as defaults]))

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

(defn- process-event [_ f e dispatch-async!]
  (f e dispatch-async!)
  nil)

(defn- print-error-handler [_ ^Throwable e]
  (.printStackTrace e))

(defn wrap-async [f options]
  (let [agent-options (-> options
                          (dissoc :sync-checker)
                          (defaults/provide :error-handler print-error-handler))
        agent (apply agent nil (mapcat identity agent-options))
        sync? (:sync-checker options :fx/sync)]
    (fn dispatch-async! [event]
      (if (sync? event)
        (f event dispatch-async!)
        (send agent process-event f event dispatch-async!)))))
