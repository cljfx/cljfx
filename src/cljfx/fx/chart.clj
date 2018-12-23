(ns cljfx.fx.chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene])
  (:import [javafx.scene.chart CategoryAxis NumberAxis PieChart PieChart$Data XYChart$Data
                               XYChart$Series AreaChart BarChart BubbleChart LineChart
                               LineChart$SortingPolicy ScatterChart StackedAreaChart
                               StackedBarChart XYChart Chart ValueAxis Axis]
           [javafx.geometry Side]))

(set! *warn-on-reflection* true)

(def axis
  (lifecycle.composite/describe Axis
    :extends [fx.scene/region]
    :props {:side [:setter prop/scalar :coerce (coerce/enum Side)]
            :label [:setter prop/scalar]
            :tick-mark-visible [:setter prop/scalar :default true]
            :tick-labels-visible [:setter prop/scalar :default true]
            :tick-length [:setter prop/scalar :coerce coerce/as-double :default 8]
            :auto-ranging [:setter prop/scalar :default true]
            :tick-label-font [:setter prop/scalar :coerce coerce/font
                              :default {:family "System" :size 8}]
            :tick-label-fill [:setter prop/scalar :coerce coerce/paint :default :black]
            :tick-label-gap [:setter prop/scalar :coerce coerce/as-double :default 3]
            :animated [:setter prop/scalar :default true]
            :tick-label-rotation [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def value-axis
  (lifecycle.composite/describe ValueAxis
    :extends [axis]
    :props {:minor-tick-visible [:setter prop/scalar :default true]
            :lower-bound [:setter prop/scalar :coerce coerce/as-double :default 0]
            :upper-bound [:setter prop/scalar :coerce coerce/as-double :default 100]
            :minor-tick-count [:setter prop/scalar :coerce coerce/as-int :default 5]
            :minor-tick-length [:setter prop/scalar :coerce coerce/as-double :default 5]
            :tick-label-formatter [:setter prop/scalar :coerce coerce/string-converter]}))

(def chart
  (lifecycle.composite/describe Chart
    :extends [fx.scene/region]
    :props {:animated [:setter prop/scalar :default true]
            :legend-side [:setter prop/scalar :coerce (coerce/enum Side) :default :bottom]
            :legend-visible [:setter prop/scalar :default true]
            :title [:setter prop/scalar]
            :title-side [:setter prop/scalar :coerce (coerce/enum Side) :default :top]}))

(def xy-chart
  (lifecycle.composite/describe XYChart
    :extends [chart]
    :props {:x-axis [(prop/ctor-only) prop/component]
            :y-axis [(prop/ctor-only) prop/component]
            :alternative-column-fill-visible [:setter prop/scalar :default false]
            :alternative-row-fill-visible [:setter prop/scalar :default true]
            :data [:list prop/component-vec]
            :horizontal-grid-lines-visible [:setter prop/scalar :default true]
            :horizontal-zero-line-visible [:setter prop/scalar :default true]
            :vertical-grid-lines-visible [:setter prop/scalar :default true]
            :vertical-zero-line-visible [:setter prop/scalar :default true]}))

(def category-axis
  (lifecycle.composite/describe CategoryAxis
    :ctor []
    :extends [axis]
    :default-prop [:categories prop/extract-all]
    :props {:categories [:list prop/scalar]
            :start-margin [:setter prop/scalar :coerce coerce/as-double :default 5.0]
            :end-margin [:setter prop/scalar :coerce coerce/as-double :default 5.0]
            :gap-start-and-end [:setter prop/scalar :default true]}))

(def number-axis
  (lifecycle.composite/describe NumberAxis
    :ctor []
    :extends [value-axis]
    :props {:force-zero-in-range [:setter prop/scalar :default true]
            :tick-unit [:setter prop/scalar :coerce coerce/as-double :default 5.0]}))

(def pie-chart
  (lifecycle.composite/describe PieChart
    :ctor []
    :extends [chart]
    :props {:clockwise [:setter prop/scalar :default true]
            :data [:list prop/component-vec]
            :label-line-length [:setter prop/scalar :coerce coerce/as-double :default 20.0]
            :labels-visible [:setter prop/scalar :default true]
            :start-angle [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def pie-chart-data
  (lifecycle.composite/describe PieChart$Data
    :ctor [:name :pie-value]
    :props {:name [:setter prop/scalar]
            :pie-value [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def xy-chart-data
  (lifecycle.composite/describe XYChart$Data
    :ctor []
    :props {:extra-value [:setter prop/scalar]
            :node [:setter prop/component]
            :x-value [:setter prop/scalar]
            :y-value [:setter prop/scalar]}))

(def xy-chart-series
  (lifecycle.composite/describe XYChart$Series
    :ctor []
    :default-prop [:data prop/extract-all]
    :props {:data [:list prop/component-vec]
            :name [:setter prop/scalar]
            :node [:setter prop/component]}))

(def area-chart
  (lifecycle.composite/describe AreaChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]
    :props {:create-symbols [:setter prop/scalar :default true]}))

(def bar-chart
  (lifecycle.composite/describe BarChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]
    :props {:bar-gap [:setter prop/scalar :coerce coerce/as-double :default 4]
            :category-gap [:setter prop/scalar :coerce coerce/as-double :default 10]}))

(def bubble-chart
  (lifecycle.composite/describe BubbleChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]))

(def line-chart
  (lifecycle.composite/describe LineChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]
    :props {:axis-sorting-policy [:setter prop/scalar
                                  :coerce (coerce/enum LineChart$SortingPolicy)
                                  :default :x-axis]
            :create-symbols [:setter prop/scalar :default true]}))

(def scatter-chart
  (lifecycle.composite/describe ScatterChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]))

(def stacked-area-chart
  (lifecycle.composite/describe StackedAreaChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]
    :props {:create-symbols [:setter prop/scalar :default true]}))

(def stacked-bar-chart
  (lifecycle.composite/describe StackedBarChart
    :ctor [:x-axis :y-axis]
    :extends [xy-chart]
    :default-prop [:data prop/extract-all]
    :props {:category-gap [:setter prop/scalar :coerce coerce/as-double :default 10]}))

(def tag->lifecycle
  {:chart.axis/category category-axis
   :chart.axis/number number-axis
   :chart.data/pie pie-chart-data
   :chart.data/xy xy-chart-data
   :chart.series/xy xy-chart-series
   :chart/pie pie-chart
   :chart/area area-chart
   :chart/bar bar-chart
   :chart/bubble bubble-chart
   :chart/line line-chart
   :chart/scatter scatter-chart
   :chart/stacked-area stacked-area-chart
   :chart/stacked-bar stacked-bar-chart})