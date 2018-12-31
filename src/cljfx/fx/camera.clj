(ns cljfx.fx.camera
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.lifecycle.composite :as lifecycle.composite])
  (:import [javafx.scene PerspectiveCamera ParallelCamera Camera]))

(def camera
  (lifecycle.composite/describe Camera
    :props {:near-clip [:setter lifecycle/scalar :coerce double :default 0.1]
            :far-clip [:setter lifecycle/scalar :coerce double :default 100]}))

(def parallel-camera
  (lifecycle.composite/describe ParallelCamera
    :ctor []
    :extends [camera]))

(def perspective-camera
  (lifecycle.composite/describe PerspectiveCamera
    :ctor []
    :extends [camera]
    :props {:field-of-view [:setter lifecycle/scalar :coerce double :default 30.0]
            :vertical-field-of-view [:setter lifecycle/scalar :default true]}))

(def keyword->lifecycle
  {:camera/parallel parallel-camera
   :camera/perspective perspective-camera})
