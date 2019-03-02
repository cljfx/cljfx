(ns cljfx.fx.color-adjust
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect ColorAdjust]))

(set! *warn-on-reflection* true)

(def props
  (composite/props ColorAdjust
    :input [:setter lifecycle/dynamic]
    :hue [:setter lifecycle/scalar :coerce double :default 0]
    :saturation [:setter lifecycle/scalar :coerce double :default 0]
    :brightness [:setter lifecycle/scalar :coerce double :default 0]
    :contrast [:setter lifecycle/scalar :coerce double :default 0]))

(def lifecycle
  (composite/describe ColorAdjust
    :ctor []
    :props props))
