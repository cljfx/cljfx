(ns cljfx.fx.stacked-bar-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.chart StackedBarChart]))

(def lifecycle
  (lifecycle.composite/describe StackedBarChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default "stacked-bar-chart"]
            ;; definitions
            :category-gap [:setter lifecycle/scalar :coerce double :default 10]}))