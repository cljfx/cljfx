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

(defn provide [m k v]
  (update m k or-default v))

(defn fill-opts [opts]
  (-> opts
      (provide :fx.opt/type->lifecycle fx-type->lifecycle)
      (provide :fx.opt/map-event-handler map-event-handler)))
