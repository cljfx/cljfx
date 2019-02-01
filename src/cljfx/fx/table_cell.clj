(ns cljfx.fx.table-cell
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TableCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe TableCell
    :ctor []
    :extends [fx.indexed-cell/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["cell" "indexed-cell" "table-cell"]]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :table-cell]}))

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [TableCell] []
      (updateItem [item empty]
        (let [^TableCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f props this item empty)))))))
