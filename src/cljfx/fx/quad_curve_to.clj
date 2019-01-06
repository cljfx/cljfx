(ns cljfx.fx.quad-curve-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape QuadCurveTo]))

(def lifecycle
  (lifecycle.composite/describe QuadCurveTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:control-x [:setter lifecycle/scalar :coerce double :default 0]
            :control-y [:setter lifecycle/scalar :coerce double :default 0]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))