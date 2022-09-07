(ns cljfx.fx.bubble-chart
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.xy-chart :as fx.xy-chart]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart BubbleChart]))

(set! *warn-on-reflection* true)

(def props
  fx.xy-chart/props)

(def lifecycle
  (lifecycle/annotate
    (composite/describe BubbleChart
      :ctor [:x-axis :y-axis]
      :props props)
    :bubble-chart))
