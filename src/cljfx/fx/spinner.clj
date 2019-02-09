(ns cljfx.fx.spinner
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Spinner]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (composite/props Spinner
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "spinner"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :spinner]
      ;; definitions
      :editable [:setter lifecycle/scalar :default false]
      :initial-delay [:setter lifecycle/scalar :coerce coerce/duration :default [300 :ms]]
      :prompt-text [:setter lifecycle/scalar :default ""]
      :repeat-delay [:setter lifecycle/scalar :default [60 :ms]]
      :value-factory [:setter lifecycle/dynamic])))

(def lifecycle
  (composite/describe Spinner
    :ctor []
    :props props))
