(ns cljfx.fx.circle
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Circle]))

(def lifecycle
  (lifecycle.composite/describe Circle
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:center-x [:setter lifecycle/scalar :coerce double :default 0]
            :center-y [:setter lifecycle/scalar :coerce double :default 0]
            :radius [:setter lifecycle/scalar :coerce double :default 0]}))