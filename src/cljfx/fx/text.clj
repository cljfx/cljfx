(ns cljfx.fx.text
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.jdk.fx.text :as jdk.fx.text]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.text Text TextBoundsType TextAlignment FontSmoothingType]
           [javafx.geometry VPos]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    jdk.fx.text/props
    (composite/props Text
      ;; overrides
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :text]
      :pick-on-bounds [:setter lifecycle/scalar :default true]
      ;; definitions
      :text [:setter lifecycle/scalar :default ""]
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
                            :default :gray])))

(def lifecycle
  (composite/describe Text
    :ctor []
    :props props))
