(ns cljfx.fx.cubic-curve
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape CubicCurve]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe CubicCurve
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:control-x1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-x2 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y2 [:setter lifecycle/scalar :coerce double :default 0]
            :end-x [:setter lifecycle/scalar :coerce double :default 0]
            :end-y [:setter lifecycle/scalar :coerce double :default 0]
            :start-x [:setter lifecycle/scalar :coerce double :default 0]
            :start-y [:setter lifecycle/scalar :coerce double :default 0]}))