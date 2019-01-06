(ns cljfx.fx.sphere
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape Sphere]))

(def lifecycle
  (lifecycle.composite/describe Sphere
    :ctor []
    :extends [fx.shape3d/lifecycle]
    :props {:radius [:setter lifecycle/scalar :coerce double :default 1.0]}))