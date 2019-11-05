(ns cljfx.fx.spinner
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.jdk.fx.spinner :as jdk.fx.spinner]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Spinner]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    jdk.fx.spinner/props
    (composite/props Spinner
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "spinner"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :spinner]
      ;; definitions
      :editable [:setter lifecycle/scalar :default false]
      :value-factory [:setter lifecycle/dynamic])))

(def lifecycle
  (composite/describe Spinner
    :ctor []
    :props props))
