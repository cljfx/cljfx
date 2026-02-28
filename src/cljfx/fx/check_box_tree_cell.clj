(ns cljfx.fx.check-box-tree-cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.tree-cell :as fx.tree-cell]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control.cell CheckBoxTreeCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.tree-cell/props
    (composite/props CheckBoxTreeCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "tree-cell" "check-box-tree-cell"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :check-box-tree-item]
      ;; definitions
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter])))
      ; TODO how to define this ??
      ; :selected-state-callback [])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe CheckBoxTreeCell
      :ctor []
      :props props)
    :check-box-tree-cell))

;; proxy-super uses reflection because updateItem is protected

(set! *warn-on-reflection* false)

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [CheckBoxTreeCell] []
      (updateItem [item empty]
        (let [^CheckBoxTreeCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f props this item empty)))))))
