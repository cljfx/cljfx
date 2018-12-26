(ns cljfx.defaults
  (:require [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]))

(defn fn-tag->lifecycle [tag]
  (when (fn? tag) lifecycle/fn-dynamic-hiccup))

(defn tag->lifecycle [tag]
  (or (fx/tag->lifecycle tag)
      (fn-tag->lifecycle tag)))

(defn map-event-handler [e]
  (prn ::unhandled-map-event e))

(def opts
  {:cljfx.opt/tag->lifecycle tag->lifecycle
   :cljfx.opt/map-event-handler map-event-handler})
