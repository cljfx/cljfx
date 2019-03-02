(ns cljfx.fx.light-spot
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.light-point :as fx.light-point])
  (:import [javafx.scene.effect Light$Spot]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.light-point/props
    (composite/props Light$Spot
      :points-at-x [:setter lifecycle/scalar :coerce double :default 0]
      :points-at-y [:setter lifecycle/scalar :coerce double :default 0]
      :points-at-z [:setter lifecycle/scalar :coerce double :default 0]
      :specular-exponent [:setter lifecycle/scalar :coerce double :default 1])))

(def lifecycle
  (composite/describe Light$Spot
    :ctor []
    :props props))
