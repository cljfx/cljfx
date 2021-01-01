(ns cljfx.fx.list-spinner-value-factory
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.spinner-value-factory :as fx.spinner-value-factory]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control SpinnerValueFactory$ListSpinnerValueFactory]
           [javafx.collections ObservableList FXCollections]
           [java.util Collection]))

(set! *warn-on-reflection* true)

(defn- observable-list [x]
  (cond
    (instance? ObservableList x) x
    (instance? Collection x) (FXCollections/observableArrayList ^Collection x)
    :else (coerce/fail ObservableList x)))

(def props
  (merge
    fx.spinner-value-factory/props
    (composite/props SpinnerValueFactory$ListSpinnerValueFactory
      :items [:list lifecycle/scalar :coerce observable-list])))

(def lifecycle
  (composite/describe SpinnerValueFactory$ListSpinnerValueFactory
    :ctor [:items]
    :props props))
