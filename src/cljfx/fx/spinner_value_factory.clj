(ns cljfx.fx.spinner-value-factory
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control SpinnerValueFactory]))

(set! *warn-on-reflection* true)

(def props
  (composite/props SpinnerValueFactory
    :converter [:setter lifecycle/scalar :coerce coerce/string-converter]
    :value [:setter lifecycle/scalar]
    :wrap-around [:setter lifecycle/scalar :default false]))
