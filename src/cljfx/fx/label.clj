(ns cljfx.fx.label
  (:require [cljfx.composite :as composite]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control Label]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.labeled/props
    (composite/props Label
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "label"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :text])))

(def lifecycle
  ;; TODO label has label-for prop - a component ref
  (composite/describe Label
    :ctor []
    :props props))
