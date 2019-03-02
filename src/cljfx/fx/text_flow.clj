(ns cljfx.fx.text-flow
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.text TextFlow TextAlignment]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    (composite/props TextFlow
      ;; overrides
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :text]
      ;; definitions
      :line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
      :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                       :default :left])))

(def lifecycle
  (composite/describe TextFlow
    :ctor []
    :props props))
