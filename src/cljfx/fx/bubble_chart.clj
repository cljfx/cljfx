(ns cljfx.fx.bubble-chart
  (:require [cljfx.composite :as composite]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart BubbleChart]))

(set! *warn-on-reflection* true)

(def props
  fx.xy-chart/props)

(def lifecycle
  (composite/describe BubbleChart
    :ctor [:x-axis :y-axis]
    :props props))
