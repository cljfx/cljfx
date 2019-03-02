(ns cljfx.fx.xy-chart-data
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart XYChart$Data]))

(set! *warn-on-reflection* true)

(def props
  (composite/props XYChart$Data
    :extra-value [:setter lifecycle/scalar]
    :node [:setter lifecycle/dynamic]
    :x-value [:setter lifecycle/scalar]
    :y-value [:setter lifecycle/scalar]))

(def lifecycle
  (composite/describe XYChart$Data
    :ctor []
    :props props))
