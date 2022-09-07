(ns cljfx.fx.tree-cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.indexed-cell :as fx.indexed-cell]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TreeCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.indexed-cell/props
    (composite/props TreeCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "tree-cell"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tree-item]
      ;; definitions
      :disclosure-node [:setter lifecycle/dynamic])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe TreeCell
      :ctor []
      :props props)
    :tree-cell))

;; proxy-super uses reflection because updateItem is protected

(set! *warn-on-reflection* false)

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [TreeCell] []
      (updateItem [item empty]
        (let [^TreeCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f props this item empty)))))))
