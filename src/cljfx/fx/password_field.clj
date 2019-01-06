(ns cljfx.fx.password-field
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.text-field :as fx.text-field])
  (:import [javafx.scene.control PasswordField]))

(def lifecycle
  (lifecycle.composite/describe PasswordField
    :ctor []
    :extends [fx.text-field/lifecycle]))