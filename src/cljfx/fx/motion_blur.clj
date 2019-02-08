(ns cljfx.fx.motion-blur
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect MotionBlur]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props MotionBlur
    :input [:setter lifecycle/dynamic]
    :radius [:setter lifecycle/scalar :coerce double :default 10]
    :angle [:setter lifecycle/scalar :coerce double :default 0]))

(def lifecycle
  (lifecycle.composite/describe MotionBlur
    :ctor []
    :props props))
