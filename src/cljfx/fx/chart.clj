(ns cljfx.fx.chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.region :as fx.region])
  (:import [javafx.scene.chart Chart]
           [javafx.geometry Side]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Chart
    :extends [fx.region/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "chart"]
            ;; definitions
            :animated [:setter lifecycle/scalar :default true]
            :legend-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :bottom]
            :legend-visible [:setter lifecycle/scalar :default true]
            :title [:setter lifecycle/scalar]
            :title-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :top]}))