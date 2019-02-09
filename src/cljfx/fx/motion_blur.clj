(ns cljfx.fx.motion-blur
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect MotionBlur]))

(set! *warn-on-reflection* true)

(def props
  (composite/props MotionBlur
    :input [:setter lifecycle/dynamic]
    :radius [:setter lifecycle/scalar :coerce double :default 10]
    :angle [:setter lifecycle/scalar :coerce double :default 0]))

(def lifecycle
  (composite/describe MotionBlur
    :ctor []
    :props props))
