(ns cljfx.fx.mesh-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape MeshView]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape3d/props
    (lifecycle.composite/props MeshView
      :mesh [:setter lifecycle/dynamic])))

(def lifecycle
  (lifecycle.composite/describe MeshView
    :ctor []
    :props props))
