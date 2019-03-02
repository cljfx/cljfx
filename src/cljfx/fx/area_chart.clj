(ns cljfx.fx.area-chart
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart AreaChart]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.xy-chart/props
    (composite/props AreaChart
      :create-symbols [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe AreaChart
    :ctor [:x-axis :y-axis]
    :props props))
