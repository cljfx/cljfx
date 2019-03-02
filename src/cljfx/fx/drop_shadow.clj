(ns cljfx.fx.drop-shadow
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect DropShadow BlurType]))

(set! *warn-on-reflection* true)

(def props
  (composite/props DropShadow
    :input [:setter lifecycle/dynamic]
    :radius [:setter lifecycle/scalar :coerce double :default 10]
    :width [:setter lifecycle/scalar :coerce double :default 21]
    :height [:setter lifecycle/scalar :coerce double :default 21]
    :blur-type [:setter lifecycle/scalar :coerce (coerce/enum BlurType)
                :default :three-pass-box]
    :spread [:setter lifecycle/scalar :coerce double :default 0]
    :color [:setter lifecycle/scalar :coerce coerce/color :default :black]
    :offset-x [:setter lifecycle/scalar :coerce double :default 0]
    :offset-y [:setter lifecycle/scalar :coerce double :default 0]))

(def lifecycle
  (composite/describe DropShadow
    :ctor []
    :props props))
