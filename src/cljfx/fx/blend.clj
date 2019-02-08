(ns cljfx.fx.blend
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect Blend BlendMode]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props Blend
    :bottom-input [:setter lifecycle/dynamic]
    :mode [:setter lifecycle/scalar :coerce (coerce/enum BlendMode)]
    :opacity [:setter lifecycle/scalar :coerce double :default 1]
    :top-input [:setter lifecycle/dynamic]))

(def lifecycle
  (lifecycle.composite/describe Blend
    :ctor []
    :props props))
