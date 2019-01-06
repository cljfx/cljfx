(ns cljfx.fx.color-picker
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.combo-box-base :as fx.combo-box-base]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control ColorPicker]))

(def lifecycle
  (lifecycle.composite/describe ColorPicker
    :ctor []
    :extends [fx.combo-box-base/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default "color-picker"]
            ;; definitions
            :value [:setter lifecycle/scalar :coerce coerce/color :default :white]
            :custom-colors [:list lifecycle/scalar :coerce #(map coerce/color %)]}))