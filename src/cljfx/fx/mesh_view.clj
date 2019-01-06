(ns cljfx.fx.mesh-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape MeshView]))

(def lifecycle
  (lifecycle.composite/describe MeshView
    :ctor []
    :extends [fx.shape3d/lifecycle]
    :props {:mesh [:setter lifecycle/dynamic]}))