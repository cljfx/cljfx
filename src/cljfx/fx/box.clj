(ns cljfx.fx.box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape Box]))

(def lifecycle
  (lifecycle.composite/describe Box
    :ctor []
    :extends [fx.shape3d/lifecycle]
    :props {:depth [:setter lifecycle/scalar :coerce double :default 2.0]
            :height [:setter lifecycle/scalar :coerce double :default 2.0]
            :width [:setter lifecycle/scalar :coerce double :default 2.0]}))