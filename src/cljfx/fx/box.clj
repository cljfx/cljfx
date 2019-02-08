(ns cljfx.fx.box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape Box]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape3d/props
    (lifecycle.composite/props Box
      :depth [:setter lifecycle/scalar :coerce double :default 2.0]
      :height [:setter lifecycle/scalar :coerce double :default 2.0]
      :width [:setter lifecycle/scalar :coerce double :default 2.0])))

(def lifecycle
  (lifecycle.composite/describe Box
    :ctor []
    :props props))
