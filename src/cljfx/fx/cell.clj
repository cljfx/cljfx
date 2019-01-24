(ns cljfx.fx.cell
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Cell]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Cell
    :ctor []
    :extends [fx.labeled/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "cell"]
            ;; definitions
            :editable [:setter lifecycle/scalar :default true]}))