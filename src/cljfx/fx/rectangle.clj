(ns cljfx.fx.rectangle
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Rectangle]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Rectangle
      :arc-height [:setter lifecycle/scalar :coerce double :default 0.0]
      :arc-width [:setter lifecycle/scalar :coerce double :default 0.0]
      :height [:setter lifecycle/scalar :coerce double :default 0.0]
      :width [:setter lifecycle/scalar :coerce double :default 0.0]
      :x [:setter lifecycle/scalar :coerce double :default 0.0]
      :y [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (composite/describe Rectangle
    :ctor []
    :props props))
