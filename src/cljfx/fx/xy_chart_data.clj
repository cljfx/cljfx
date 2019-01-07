(ns cljfx.fx.xy-chart-data
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart XYChart$Data]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe XYChart$Data
    :ctor []
    :props {:extra-value [:setter lifecycle/scalar]
            :node [:setter lifecycle/dynamic]
            :x-value [:setter lifecycle/scalar]
            :y-value [:setter lifecycle/scalar]}))