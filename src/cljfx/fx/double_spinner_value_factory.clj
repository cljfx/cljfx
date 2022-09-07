(ns cljfx.fx.double-spinner-value-factory
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.spinner-value-factory :as fx.spinner-value-factory])
  (:import [javafx.scene.control SpinnerValueFactory$DoubleSpinnerValueFactory]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.spinner-value-factory/props
    (composite/props SpinnerValueFactory$DoubleSpinnerValueFactory
      :amount-to-step-by [:setter lifecycle/scalar :coerce double :default 1]
      :value [:setter lifecycle/scalar :coerce double]
      :max [:setter lifecycle/scalar :coerce double :default 100]
      :min [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe SpinnerValueFactory$DoubleSpinnerValueFactory
      :ctor [:min :max]
      :props props)
    :double-spinner-value-factory))
