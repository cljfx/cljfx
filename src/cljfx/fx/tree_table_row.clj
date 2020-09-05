(ns cljfx.fx.tree-table-row
  "Part of a public API"
  (:require [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TreeTableRow]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.indexed-cell/props
    (composite/props TreeTableRow
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "table-row-cell"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tree-table-row]
      ;; definitions
      :disclosure-node [:setter lifecycle/dynamic])))

(def lifecycle
  (composite/describe TreeTableRow
    :ctor []
    :props props))