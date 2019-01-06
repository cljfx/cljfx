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
    :props {:value [:setter lifecycle/scalar :coerce coerce/color]
            :custom-colors [:list lifecycle/scalar :coerce #(map coerce/color %)]}))