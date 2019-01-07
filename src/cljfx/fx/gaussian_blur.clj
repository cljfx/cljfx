(ns cljfx.fx.gaussian-blur
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect GaussianBlur]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe GaussianBlur
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :radius [:setter lifecycle/scalar :coerce double :default 10]}))