(ns cljfx.fx.cubic-curve-to
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape CubicCurveTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (composite/props CubicCurveTo
      :control-x1 [:setter lifecycle/scalar :coerce double :default 0]
      :control-x2 [:setter lifecycle/scalar :coerce double :default 0]
      :control-y1 [:setter lifecycle/scalar :coerce double :default 0]
      :control-y2 [:setter lifecycle/scalar :coerce double :default 0]
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe CubicCurveTo
    :ctor []
    :props props))
