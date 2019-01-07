(ns cljfx.fx.double-spinner-value-factory
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.spinner-value-factory :as fx.spinner-value-factory])
  (:import [javafx.scene.control SpinnerValueFactory$DoubleSpinnerValueFactory]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe SpinnerValueFactory$DoubleSpinnerValueFactory
    :ctor [:min :max]
    :extends [fx.spinner-value-factory/lifecycle]
    :props {:amount-to-step-by [:setter lifecycle/scalar :coerce double :default 1]
            :value [:setter lifecycle/scalar :coerce double]
            :max [:setter lifecycle/scalar :coerce double :default 100]
            :min [:setter lifecycle/scalar :coerce double :default 0]}))