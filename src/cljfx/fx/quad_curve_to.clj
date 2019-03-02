(ns cljfx.fx.quad-curve-to
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape QuadCurveTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (composite/props QuadCurveTo
      :control-x [:setter lifecycle/scalar :coerce double :default 0]
      :control-y [:setter lifecycle/scalar :coerce double :default 0]
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe QuadCurveTo
    :ctor []
    :props props))
