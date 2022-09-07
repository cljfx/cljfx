(ns cljfx.fx.password-field
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.text-field :as fx.text-field]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control PasswordField]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.text-field/props
    (composite/props PasswordField
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["text-input" "text-field" "password-field"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :password-field])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe PasswordField
      :ctor []
      :props props)
    :password-field))
