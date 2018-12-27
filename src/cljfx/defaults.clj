(ns cljfx.defaults
  (:require [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]))

(defn fn-tag->lifecycle [tag]
  (when (fn? tag) lifecycle/fn-dynamic-hiccup))

(defn- tag->lifecycle [tag]
  (or (fx/tag->lifecycle tag)
      (fn-tag->lifecycle tag)))

(defn- map-event-handler [e]
  (prn ::unhandled-map-event e))

(defn- or-default [x y]
  (or x y))

(defn fill-opts [opts]
  (-> opts
      (update :cljfx.opt/tag->lifecycle or-default tag->lifecycle)
      (update :cljfx.opt/map-event-handler or-default map-event-handler)))
