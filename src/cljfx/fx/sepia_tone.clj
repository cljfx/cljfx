(ns cljfx.fx.sepia-tone
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect SepiaTone]))

(set! *warn-on-reflection* true)

(def props
  (composite/props SepiaTone
    :input [:setter lifecycle/dynamic]
    :level [:setter lifecycle/scalar :coerce double :default 1.0]))

(def lifecycle
  (composite/describe SepiaTone
    :ctor []
    :props props))
