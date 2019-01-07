(ns cljfx.fx.box-blur
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect BoxBlur]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe BoxBlur
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :iterations [:setter lifecycle/scalar :coerce int :default 1]
            :width [:setter lifecycle/scalar :coerce double :default 5]
            :height [:setter lifecycle/scalar :coerce double :default 5]}))