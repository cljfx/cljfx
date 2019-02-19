(ns cljfx.fx.light-point
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.light :as fx.light])
  (:import [javafx.scene.effect Light$Point]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.light/props
    (composite/props Light$Point
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0]
      :z [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe Light$Point
    :ctor []
    :props props))
