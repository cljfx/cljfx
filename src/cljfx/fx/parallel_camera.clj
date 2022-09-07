(ns cljfx.fx.parallel-camera
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.camera :as fx.camera]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene ParallelCamera]))

(set! *warn-on-reflection* true)

(def props
  fx.camera/props)

(def lifecycle
  (lifecycle/annotate
    (composite/describe ParallelCamera
      :ctor []
      :props props)
    :parallel-camera))
