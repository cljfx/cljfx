(ns cljfx.fx.label
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control Label]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  ;; TODO label has label-for prop - a component ref
  (lifecycle.composite/describe Label
    :ctor []
    :extends [fx.labeled/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "label"]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :text]}))
