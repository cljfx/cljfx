(ns cljfx.fx.point-light-effect
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.light :as fx.light])
  (:import [javafx.scene.effect Light$Point]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Light$Point
    :ctor []
    :extends [fx.light/lifecycle]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]
            :z [:setter lifecycle/scalar :coerce double :default 0]}))