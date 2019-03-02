(ns cljfx.fx.bar-chart
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.chart BarChart]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.xy-chart/props
    (composite/props BarChart
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "bar-chart"]
      ;; definitions
      :bar-gap [:setter lifecycle/scalar :coerce double :default 4]
      :category-gap [:setter lifecycle/scalar :coerce double :default 10])))

(def lifecycle
  (composite/describe BarChart
    :ctor [:x-axis :y-axis]
    :props props))
