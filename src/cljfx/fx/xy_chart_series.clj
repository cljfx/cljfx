(ns cljfx.fx.xy-chart-series
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart XYChart$Series]))

(def lifecycle
  (lifecycle.composite/describe XYChart$Series
    :ctor []
    :props {:data [:list lifecycle/dynamics]
            :name [:setter lifecycle/scalar]
            :node [:setter lifecycle/dynamic]}))