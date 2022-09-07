(ns cljfx.fx.pie-chart
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.chart :as fx.chart])
  (:import [javafx.scene.chart PieChart]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.chart/props
    (composite/props PieChart
      :clockwise [:setter lifecycle/scalar :default true]
      :data [:list lifecycle/dynamics]
      :label-line-length [:setter lifecycle/scalar :coerce double :default 20.0]
      :labels-visible [:setter lifecycle/scalar :default true]
      :start-angle [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe PieChart
      :ctor []
      :props props)
    :pie-chart))
