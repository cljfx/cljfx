(ns cljfx.fx.indexed-cell
  (:require [cljfx.composite :as composite]
            [cljfx.fx.cell :as fx.cell]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control IndexedCell]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.cell/props
    (composite/props IndexedCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell"]])))

(def lifecycle
  (composite/describe IndexedCell
    :ctor []
    :props props))
