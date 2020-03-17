(ns cljfx.defaults
  "Part of a public API"
  (:require [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.event-handler :as event-handler]))

(defn fn->lifecycle [fx-type]
  (when (or (fn? fx-type)
            (instance? clojure.lang.MultiFn fx-type))
    lifecycle/dynamic-fn->dynamic))

(defn- fx-type->lifecycle [fx-type]
  (or (fx/keyword->lifecycle fx-type)
      (fn->lifecycle fx-type)))

(defn- map-event-handler [e]
  (prn ::unhandled-map-event e))

(defmacro provide [m k v]
  `(let [m# ~m
         k# ~k]
     (if (contains? m# k#)
       m#
       (assoc m# k# ~v))))

(defn fill-opts [opts]
  (-> opts
      (provide :fx.opt/type->lifecycle fx-type->lifecycle)
      (provide :fx.opt/map-event-handler map-event-handler)))

(defn fill-co-effects [co-effects *context]
  (provide co-effects :fx/context (event-handler/make-deref-co-effect *context)))

(defn fill-effects [effects *context]
  (-> effects
      (provide :context (event-handler/make-reset-effect *context))
      (provide :dispatch event-handler/dispatch-effect)))

(defn- print-error-handler [_ ^Throwable e]
  (.printStackTrace e))

(defn fill-async-handler-options [agent-options]
  (provide agent-options :error-handler print-error-handler))
