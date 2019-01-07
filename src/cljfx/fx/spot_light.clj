(ns cljfx.fx.spot-light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.point-light-effect :as fx.point-light-effect])
  (:import [javafx.scene.effect Light$Spot]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Light$Spot
    :ctor []
    :extends [fx.point-light-effect/lifecycle]
    :props {:points-at-x [:setter lifecycle/scalar :coerce double :default 0]
            :points-at-y [:setter lifecycle/scalar :coerce double :default 0]
            :points-at-z [:setter lifecycle/scalar :coerce double :default 0]
            :specular-exponent [:setter lifecycle/scalar :coerce double :default 1]}))