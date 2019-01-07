(ns cljfx.fx.password-field
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.text-field :as fx.text-field]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control PasswordField]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe PasswordField
    :ctor []
    :extends [fx.text-field/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["text-input" "text-field" "password-field"]]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :password-field]}))