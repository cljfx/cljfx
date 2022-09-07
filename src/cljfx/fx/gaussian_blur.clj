(ns cljfx.fx.gaussian-blur
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect GaussianBlur]))

(set! *warn-on-reflection* true)

(def props
  (composite/props GaussianBlur
    :input [:setter lifecycle/dynamic]
    :radius [:setter lifecycle/scalar :coerce double :default 10]))

(def lifecycle
  (lifecycle/annotate
    (composite/describe GaussianBlur
      :ctor []
      :props props)
    :gaussian-blur))
