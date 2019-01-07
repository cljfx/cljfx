(ns cljfx.fx.pie-chart-data
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart PieChart$Data]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe PieChart$Data
    :ctor [:name :pie-value]
    :props {:name [:setter lifecycle/scalar]
            :pie-value [:setter lifecycle/scalar :coerce double :default 0]}))