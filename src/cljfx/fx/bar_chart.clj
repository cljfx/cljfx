(ns cljfx.fx.bar-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart BarChart]))

(def lifecycle
  (lifecycle.composite/describe BarChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]
    :props {:bar-gap [:setter lifecycle/scalar :coerce double :default 4]
            :category-gap [:setter lifecycle/scalar :coerce double :default 10]}))