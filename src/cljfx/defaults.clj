(ns cljfx.defaults
  (:require [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]))

(defn fn->lifecycle [fx-type]
  (when (fn? fx-type) lifecycle/dynamic-fn->dynamic))

(defn- fx-type->lifecycle [fx-type]
  (or (fx/keyword->lifecycle fx-type)
      (fn->lifecycle fx-type)))

(defn- map-event-handler [e]
  (prn ::unhandled-map-event e))

(defn- or-default [x y]
  (or x y))

(defn fill-opts [opts]
  (-> opts
      (update :fx.opt/type->lifecycle or-default fx-type->lifecycle)
      (update :fx.opt/map-event-handler or-default map-event-handler)))
