(ns cljfx.fx.hyperlink
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.button-base :as fx.button-base]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Hyperlink]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Hyperlink
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "hyperlink"]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :hyperlink]
            ;; definitions
            :visited [:setter lifecycle/scalar :default false]}))