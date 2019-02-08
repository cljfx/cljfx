(ns cljfx.fx.gaussian-blur
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect GaussianBlur]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props GaussianBlur
    :input [:setter lifecycle/dynamic]
    :radius [:setter lifecycle/scalar :coerce double :default 10]))

(def lifecycle
  (lifecycle.composite/describe GaussianBlur
    :ctor []
    :props props))
