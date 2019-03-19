(ns cljfx.fx.tree-table-cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TreeTableCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.indexed-cell/props
    (composite/props TreeTableCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "tree-table-cell"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tree-table-cell])))

(def lifecycle
  (composite/describe TreeTableCell
    :ctor []
    :props props))

;; proxy-super uses reflection because updateItem is protected

(set! *warn-on-reflection* false)

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [TreeTableCell] []
      (updateItem [item empty]
        (let [^TreeTableCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f props this item empty)))))))
