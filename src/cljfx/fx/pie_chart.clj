(ns cljfx.fx.pie-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.chart :as fx.chart])
  (:import [javafx.scene.chart PieChart]))

(def lifecycle
  (lifecycle.composite/describe PieChart
    :ctor []
    :extends [fx.chart/lifecycle]
    :props {:clockwise [:setter lifecycle/scalar :default true]
            :data [:list lifecycle/dynamics]
            :label-line-length [:setter lifecycle/scalar :coerce double :default 20.0]
            :labels-visible [:setter lifecycle/scalar :default true]
            :start-angle [:setter lifecycle/scalar :coerce double :default 0.0]}))