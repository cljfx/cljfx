(ns cljfx.fx.box-blur
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect BoxBlur]))

(set! *warn-on-reflection* true)

(def props
  (composite/props BoxBlur
    :input [:setter lifecycle/dynamic]
    :iterations [:setter lifecycle/scalar :coerce int :default 1]
    :width [:setter lifecycle/scalar :coerce double :default 5]
    :height [:setter lifecycle/scalar :coerce double :default 5]))

(def lifecycle
  (composite/describe BoxBlur
    :ctor []
    :props props))
