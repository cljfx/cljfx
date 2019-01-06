(ns cljfx.fx.text
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.text Text TextBoundsType TextAlignment FontSmoothingType]
           [javafx.geometry VPos]))

(def lifecycle
  (lifecycle.composite/describe Text
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:text [:setter lifecycle/scalar :default ""]
            :x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]
            :font [:setter lifecycle/scalar :coerce coerce/font]
            :text-origin [:setter lifecycle/scalar :coerce (coerce/enum VPos)
                          :default :baseline]
            :bounds-type [:setter lifecycle/scalar :coerce (coerce/enum TextBoundsType)
                          :default :logical]
            :wrapping-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :underline [:setter lifecycle/scalar :default false]
            :strikethrough [:setter lifecycle/scalar :default false]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]
            :line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
            :font-smoothing-type [:setter lifecycle/scalar
                                  :coerce (coerce/enum FontSmoothingType)
                                  :default :gray]
            :selection-start [:setter lifecycle/scalar :coerce int :default -1]
            :selection-end [:setter lifecycle/scalar :coerce int :default -1]
            :selection-fill [:setter lifecycle/scalar :coerce coerce/paint :default :white]
            :caret-position [:setter lifecycle/scalar :coerce int :default -1]
            :caret-bias [:setter lifecycle/scalar :default true]}))