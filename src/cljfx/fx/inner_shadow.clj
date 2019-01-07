(ns cljfx.fx.inner-shadow
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect InnerShadow BlurType]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe InnerShadow
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :radius [:setter lifecycle/scalar :coerce double :default 10]
            :width [:setter lifecycle/scalar :coerce double :default 21]
            :height [:setter lifecycle/scalar :coerce double :default 21]
            :blur-type [:setter lifecycle/scalar
                        :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :choke [:setter lifecycle/scalar :coerce double :default 0]
            :color [:setter lifecycle/scalar :coerce coerce/color :default :black]
            :offset-x [:setter lifecycle/scalar :coerce double :default 0]
            :offset-y [:setter lifecycle/scalar :coerce double :default 0]}))