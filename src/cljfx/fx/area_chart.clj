(ns cljfx.fx.area-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart AreaChart]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe AreaChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]
    :props {:create-symbols [:setter lifecycle/scalar :default true]}))