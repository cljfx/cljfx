(ns cljfx.fx.color-input
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect ColorInput]))

(set! *warn-on-reflection* true)

(def props
  (composite/props ColorInput
    :width [:setter lifecycle/scalar :coerce double :default 0]
    :height [:setter lifecycle/scalar :coerce double :default 0]
    :x [:setter lifecycle/scalar :coerce double :default 0]
    :y [:setter lifecycle/scalar :coerce double :default 0]
    :paint [:setter lifecycle/scalar :coerce coerce/paint]))

(def lifecycle
  (composite/describe ColorInput
    :ctor []
    :props props))
