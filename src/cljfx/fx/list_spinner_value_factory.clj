(ns cljfx.fx.list-spinner-value-factory
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.spinner-value-factory :as fx.spinner-value-factory]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control SpinnerValueFactory$ListSpinnerValueFactory]))

(def lifecycle
  (lifecycle.composite/describe SpinnerValueFactory$ListSpinnerValueFactory
    :ctor [:items]
    :extends [fx.spinner-value-factory/lifecycle]
    :props {:items [:list lifecycle/scalar :coerce coerce/observable-list]}))