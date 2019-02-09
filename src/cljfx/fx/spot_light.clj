(ns cljfx.fx.spot-light
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.point-light-effect :as fx.point-light-effect])
  (:import [javafx.scene.effect Light$Spot]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.point-light-effect/props
    (composite/props Light$Spot
      :points-at-x [:setter lifecycle/scalar :coerce double :default 0]
      :points-at-y [:setter lifecycle/scalar :coerce double :default 0]
      :points-at-z [:setter lifecycle/scalar :coerce double :default 0]
      :specular-exponent [:setter lifecycle/scalar :coerce double :default 1])))

(def lifecycle
  (composite/describe Light$Spot
    :ctor []
    :props props))
