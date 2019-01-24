(ns cljfx.fx.text-field-list-cell
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.list-cell :as fx.list-cell]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control.cell TextFieldListCell]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe TextFieldListCell
    :ctor []
    :extends [fx.list-cell/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default
                          ["cell" "indexed-cell" "list-cell" "text-field-list-cell"]]
            ;; definitions
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter]}))
