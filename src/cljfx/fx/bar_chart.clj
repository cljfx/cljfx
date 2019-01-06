(ns cljfx.fx.bar-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.chart BarChart]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe BarChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "bar-chart"]
            ;; definitions
            :bar-gap [:setter lifecycle/scalar :coerce double :default 4]
            :category-gap [:setter lifecycle/scalar :coerce double :default 10]}))