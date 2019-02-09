(ns cljfx.fx.circle
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Circle]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Circle
      :center-x [:setter lifecycle/scalar :coerce double :default 0]
      :center-y [:setter lifecycle/scalar :coerce double :default 0]
      :radius [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe Circle
    :ctor []
    :props props))
