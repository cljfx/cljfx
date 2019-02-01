(ns cljfx.fx.tree-table-cell
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TreeTableCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe TreeTableCell
    :ctor []
    :extends [fx.indexed-cell/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["cell" "indexed-cell" "tree-table-cell"]]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :tree-table-cell]}))

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [TreeTableCell] []
      (updateItem [item empty]
        (let [^TreeTableCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f props this item empty)))))))