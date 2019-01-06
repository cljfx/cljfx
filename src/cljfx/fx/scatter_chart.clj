(ns cljfx.fx.scatter-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart ScatterChart]))

(def lifecycle
  (lifecycle.composite/describe ScatterChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]))