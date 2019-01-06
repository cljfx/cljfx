(ns cljfx.fx.button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.button-base :as fx.button-base]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Button]
           [javafx.scene AccessibleRole]))

(def lifecycle
  (lifecycle.composite/describe Button
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar
                          :coerce coerce/style-class
                          :default "button"]
            :accessible-role [:setter lifecycle/scalar
                              :coerce (coerce/enum AccessibleRole)
                              :default :button]
            :mnemonic-parsing [:setter lifecycle/scalar :default true]
            ;; definitions
            :cancel-button [:setter lifecycle/scalar :default false]
            :default-button [:setter lifecycle/scalar :default false]}))