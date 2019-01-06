(ns cljfx.fx.toggle-button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.button-base :as fx.button-base]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control ToggleButton]
           [javafx.scene AccessibleRole]))

(def lifecycle
  (lifecycle.composite/describe ToggleButton
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "toggle-button"]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :toggle-button]
            :mnemonic-parsing [:setter lifecycle/scalar :default true]
            ;; definitions
            :selected [:setter lifecycle/scalar :default false]
            :toggle-group [:setter lifecycle/scalar]}))