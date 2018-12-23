(ns cljfx.fx.camera
  (:require [cljfx.coerce :as coerce]
            [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop])
  (:import [javafx.scene PerspectiveCamera ParallelCamera Camera]))

(def camera
  (lifecycle.composite/describe Camera
    :props {:near-clip [:setter prop/scalar :coerce coerce/as-double :default 0.1]
            :far-clip [:setter prop/scalar :coerce coerce/as-double :default 100]}))

(def parallel-camera
  (lifecycle.composite/describe ParallelCamera
    :ctor []
    :extends [camera]))

(def perspective-camera
  (lifecycle.composite/describe PerspectiveCamera
    :ctor []
    :extends [camera]
    :props {:field-of-view [:setter prop/scalar :coerce coerce/as-double :default 30.0]
            :vertical-field-of-view [:setter prop/scalar :default true]}))

(def tag->lifecycle
  {:camera/parallel parallel-camera
   :camera/perspective perspective-camera})
