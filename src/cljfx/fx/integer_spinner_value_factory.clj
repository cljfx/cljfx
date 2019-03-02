(ns cljfx.fx.integer-spinner-value-factory
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.spinner-value-factory :as fx.spinner-value-factory]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control SpinnerValueFactory$IntegerSpinnerValueFactory]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.spinner-value-factory/props
    (composite/props SpinnerValueFactory$IntegerSpinnerValueFactory
      :amount-to-step-by [:setter lifecycle/scalar :coerce int :default 1]
      :value [:setter lifecycle/scalar :coerce int]
      :max [:setter lifecycle/scalar :coerce int :default 100]
      :min [:setter lifecycle/scalar :coerce int :default 0])))

(def lifecycle
  (composite/describe SpinnerValueFactory$IntegerSpinnerValueFactory
    :ctor [:min :max]
    :props props))
