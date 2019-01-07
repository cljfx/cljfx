(ns cljfx.fx.radio-button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.toggle-button :as fx.toggle-button]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control RadioButton]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe RadioButton
    :ctor []
    :extends [fx.toggle-button/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "radio-button"]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :radio-button]}))