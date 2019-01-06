(ns cljfx.fx.cubic-curve-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape CubicCurveTo]))

(def lifecycle
  (lifecycle.composite/describe CubicCurveTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:control-x1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-x2 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y2 [:setter lifecycle/scalar :coerce double :default 0]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))