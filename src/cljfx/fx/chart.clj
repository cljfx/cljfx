(ns cljfx.fx.chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.chart CategoryAxis NumberAxis PieChart PieChart$Data XYChart$Data
                               XYChart$Series AreaChart BarChart BubbleChart LineChart
                               LineChart$SortingPolicy ScatterChart StackedAreaChart
                               StackedBarChart XYChart Chart ValueAxis Axis]
           [javafx.geometry Side]))

(set! *warn-on-reflection* true)

(def axis
  (lifecycle.composite/describe Axis
    :extends [fx.scene/region]
    :props {:side [:setter lifecycle/scalar :coerce (coerce/enum Side)]
            :label [:setter lifecycle/scalar]
            :tick-mark-visible [:setter lifecycle/scalar :default true]
            :tick-labels-visible [:setter lifecycle/scalar :default true]
            :tick-length [:setter lifecycle/scalar :coerce double :default 8]
            :auto-ranging [:setter lifecycle/scalar :default true]
            :tick-label-font [:setter lifecycle/scalar :coerce coerce/font
                              :default {:family "System" :size 8}]
            :tick-label-fill [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :tick-label-gap [:setter lifecycle/scalar :coerce double :default 3]
            :animated [:setter lifecycle/scalar :default true]
            :tick-label-rotation [:setter lifecycle/scalar :coerce double :default 0]}))

(def value-axis
  (lifecycle.composite/describe ValueAxis
    :extends [axis]
    :props {:minor-tick-visible [:setter lifecycle/scalar :default true]
            :lower-bound [:setter lifecycle/scalar :coerce double :default 0]
            :upper-bound [:setter lifecycle/scalar :coerce double :default 100]
            :minor-tick-count [:setter lifecycle/scalar :coerce int :default 5]
            :minor-tick-length [:setter lifecycle/scalar :coerce double :default 5]
            :tick-label-formatter [:setter lifecycle/scalar :coerce coerce/string-converter]}))

(def chart
  (lifecycle.composite/describe Chart
    :extends [fx.scene/region]
    :props {:animated [:setter lifecycle/scalar :default true]
            :legend-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :bottom]
            :legend-visible [:setter lifecycle/scalar :default true]
            :title [:setter lifecycle/scalar]
            :title-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :top]}))

(def xy-chart
  (lifecycle.composite/describe XYChart
    :extends [chart]
    :props {:x-axis [mutator/forbidden lifecycle/dynamic]
            :y-axis [mutator/forbidden lifecycle/dynamic]
            :alternative-column-fill-visible [:setter lifecycle/scalar :default false]
            :alternative-row-fill-visible [:setter lifecycle/scalar :default true]
            :data [:list lifecycle/dynamics]
            :horizontal-grid-lines-visible [:setter lifecycle/scalar :default true]
            :horizontal-zero-line-visible [:setter lifecycle/scalar :default true]
            :vertical-grid-lines-visible [:setter lifecycle/scalar :default true]
            :vertical-zero-line-visible [:setter lifecycle/scalar :default true]}))

(def category-axis
  (lifecycle.composite/describe CategoryAxis
    :ctor []
    :extends [axis]
    :props {:categories [:list lifecycle/scalar]
            :start-margin [:setter lifecycle/scalar :coerce double :default 5.0]
            :end-margin [:setter lifecycle/scalar :coerce double :default 5.0]
            :gap-start-and-end [:setter lifecycle/scalar :default true]}))

(def number-axis
  (lifecycle.composite/describe NumberAxis
    :ctor []
    :extends [value-axis]
    :props {:force-zero-in-range [:setter lifecycle/scalar :default true]
            :tick-unit [:setter lifecycle/scalar :coerce double :default 5.0]}))

(def pie-chart
  (lifecycle.composite/describe PieChart
    :ctor []
    :extends [chart]
    :props {:clockwise [:setter lifecycle/scalar :default true]
            :data [:list lifecycle/dynamics]
            :label-line-length [:setter lifecycle/scalar :coerce double :default 20.0]
            :labels-visible [:setter lifecycle/scalar :default true]
            :start-angle [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def pie-chart-data
  (lifecycle.composite/describe PieChart$Data
    :ctor [:name :pie-value]
    :props {:name [:setter lifecycle/scalar]
            :pie-value [:setter lifecycle/scalar :coerce double :default 0]}))

(def xy-chart-data
  (lifecycle.composite/describe XYChart$Data
    :ctor []
    :props {:extra-value [:setter lifecycle/scalar]
            :node [:setter lifecycle/dynamic]
            :x-value [:setter lifecycle/scalar]
            :y-value [:setter lifecycle/scalar]}))

(def xy-chart-series
  (lifecycle.composite/describe XYChart$Series
    :ctor []
    :props {:data [:list lifecycle/dynamics]
            :name [:setter lifecycle/scalar]
            :node [:setter lifecycle/dynamic]}))

(def area-chart
  (lifecycle.composite/describe AreaChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :props {:create-symbols [:setter lifecycle/scalar :default true]}))

(def bar-chart
  (lifecycle.composite/describe BarChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :props {:bar-gap [:setter lifecycle/scalar :coerce double :default 4]
            :category-gap [:setter lifecycle/scalar :coerce double :default 10]}))

(def bubble-chart
  (lifecycle.composite/describe BubbleChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]))

(def line-chart
  (lifecycle.composite/describe LineChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :props {:axis-sorting-policy [:setter lifecycle/scalar
                                  :coerce (coerce/enum LineChart$SortingPolicy)
                                  :default :x-axis]
            :create-symbols [:setter lifecycle/scalar :default true]}))

(def scatter-chart
  (lifecycle.composite/describe ScatterChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]))

(def stacked-area-chart
  (lifecycle.composite/describe StackedAreaChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :props {:create-symbols [:setter lifecycle/scalar :default true]}))

(def stacked-bar-chart
  (lifecycle.composite/describe StackedBarChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :props {:category-gap [:setter lifecycle/scalar :coerce double :default 10]}))

(def keyword->lifecycle
  {:category-axis category-axis
   :number-axis number-axis
   :pie-chart-data pie-chart-data
   :xy-chart-data xy-chart-data
   :xy-chart-series xy-chart-series
   :pie-chart pie-chart
   :area-chart area-chart
   :bar-chart bar-chart
   :bubble-chart bubble-chart
   :line-chart line-chart
   :scatter-chart scatter-chart
   :stacked-area-chart stacked-area-chart
   :stacked-bar-chart stacked-bar-chart})