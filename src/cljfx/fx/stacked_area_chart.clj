(ns cljfx.fx.stacked-area-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart StackedAreaChart]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.xy-chart/props
    (lifecycle.composite/props StackedAreaChart
      :create-symbols [:setter lifecycle/scalar :default true])))

(def lifecycle
  (lifecycle.composite/describe StackedAreaChart
    :ctor [:x-axis :y-axis]
    :props props))
