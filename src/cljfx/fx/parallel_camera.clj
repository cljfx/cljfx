(ns cljfx.fx.parallel-camera
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.camera :as fx.camera])
  (:import [javafx.scene ParallelCamera]))

(set! *warn-on-reflection* true)

(def props
  fx.camera/props)

(def lifecycle
  (lifecycle.composite/describe ParallelCamera
    :ctor []
    :props props))
