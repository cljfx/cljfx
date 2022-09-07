(ns cljfx.fx.toggle-button
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.button-base :as fx.button-base]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control ToggleButton ToggleGroup]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.button-base/props
    (composite/props ToggleButton
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "toggle-button"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :toggle-button]
      :mnemonic-parsing [:setter lifecycle/scalar :default true]
      ;; definitions
      :selected [:setter lifecycle/scalar :default false]
      :on-selected-changed [:property-change-listener lifecycle/change-listener]
      :toggle-group [:setter (lifecycle/if-desc #(instance? ToggleGroup %)
                               lifecycle/scalar
                               lifecycle/dynamic)])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe ToggleButton
      :ctor []
      :props props)
    :toggle-button))
