(ns cljfx.fx.xy-chart-series
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart XYChart$Series]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props XYChart$Series
    :data [:list lifecycle/dynamics]
    :name [:setter lifecycle/scalar]
    :node [:setter lifecycle/dynamic]))

(def lifecycle
  (lifecycle.composite/describe XYChart$Series
    :ctor []
    :props props))
