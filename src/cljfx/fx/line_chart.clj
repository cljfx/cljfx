(ns cljfx.fx.line-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.xy-chart :as fx.xy-chart])
  (:import [javafx.scene.chart LineChart LineChart$SortingPolicy]))

(def lifecycle
  (lifecycle.composite/describe LineChart
    :ctor [:x-axis :y-axis]
    :extends [fx.xy-chart/lifecycle]
    :props {:axis-sorting-policy [:setter lifecycle/scalar
                                  :coerce (coerce/enum LineChart$SortingPolicy)
                                  :default :x-axis]
            :create-symbols [:setter lifecycle/scalar :default true]}))