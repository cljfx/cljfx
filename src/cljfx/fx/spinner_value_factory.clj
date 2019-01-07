(ns cljfx.fx.spinner-value-factory
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control SpinnerValueFactory]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe SpinnerValueFactory
    :props {:converter [:setter lifecycle/scalar :coerce coerce/string-converter]
            :value [:setter lifecycle/scalar]
            :wrap-around [:setter lifecycle/scalar :default false]}))