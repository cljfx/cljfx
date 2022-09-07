(ns cljfx.fx.table-cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TableCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.indexed-cell/props
    (composite/props TableCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "table-cell"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :table-cell])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe TableCell
      :ctor []
      :props props)
    :table-cell))

;; proxy-super uses reflection because updateItem is protected

(set! *warn-on-reflection* false)

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [TableCell] []
      (updateItem [item empty]
        (let [^TableCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f props this item empty)))))))
