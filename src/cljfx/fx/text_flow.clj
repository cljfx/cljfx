(ns cljfx.fx.text-flow
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.jdk.fx.text-flow :as jdk.fx.text-flow]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.text TextFlow TextAlignment]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    jdk.fx.text-flow/props
    (composite/props TextFlow
      ;; overrides
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :text]
      ;; definitions
      :line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
      :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                       :default :left])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe TextFlow
      :ctor []
      :props props)
    :text-flow))
