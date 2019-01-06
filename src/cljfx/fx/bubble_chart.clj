(ns cljfx.fx.bubble-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart BubbleChart]))

(def lifecycle
  (lifecycle.composite/describe BubbleChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]))