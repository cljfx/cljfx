(ns cljfx.fx.cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Cell]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.labeled/props
    (composite/props Cell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "cell"]
      ;; definitions
      :editable [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe Cell
    :ctor []
    :props props))
