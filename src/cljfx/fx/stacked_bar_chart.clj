(ns cljfx.fx.stacked-bar-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart StackedBarChart]))

(def lifecycle
  (lifecycle.composite/describe StackedBarChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]
    :props {:category-gap [:setter lifecycle/scalar :coerce double :default 10]}))