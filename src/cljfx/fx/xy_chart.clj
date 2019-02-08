(ns cljfx.fx.xy-chart
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.chart :as fx.chart]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.chart XYChart]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.chart/props
    (lifecycle.composite/props XYChart
      :x-axis [mutator/forbidden lifecycle/dynamic]
      :y-axis [mutator/forbidden lifecycle/dynamic]
      :alternative-column-fill-visible [:setter lifecycle/scalar :default false]
      :alternative-row-fill-visible [:setter lifecycle/scalar :default true]
      :data [:list lifecycle/dynamics]
      :horizontal-grid-lines-visible [:setter lifecycle/scalar :default true]
      :horizontal-zero-line-visible [:setter lifecycle/scalar :default true]
      :vertical-grid-lines-visible [:setter lifecycle/scalar :default true]
      :vertical-zero-line-visible [:setter lifecycle/scalar :default true])))
