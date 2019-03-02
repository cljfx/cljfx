(ns cljfx.fx.line-chart
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart LineChart LineChart$SortingPolicy]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.xy-chart/props
    (composite/props LineChart
      :axis-sorting-policy [:setter lifecycle/scalar
                            :coerce (coerce/enum LineChart$SortingPolicy)
                            :default :x-axis]
      :create-symbols [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe LineChart
    :ctor [:x-axis :y-axis]
    :props props))
