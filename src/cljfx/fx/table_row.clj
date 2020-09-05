(ns cljfx.fx.table-row
  "Part of a public API"
  (:require [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TableRow]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.indexed-cell/props
    (composite/props TableRow
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "table-row-cell"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :table-row])))

(def lifecycle
  (composite/describe TableRow
    :ctor []
    :props props))