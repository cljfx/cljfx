(ns cljfx.fx.check-box-tree-cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.tree-cell :as fx.tree-cell]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.control.cell CheckBoxTreeCell]
           [javafx.scene AccessibleRole]
           [javafx.scene.control Labeled]))

(def text-key "cljfx-text")
(def graphics-key "cljfx-graphic")

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
      :text [(mutator/setter (fn [^Labeled cell x]
                               (.setText cell x)
                               (.put (.getProperties cell) text-key x)))
             lifecycle/scalar
             :default ""]
      :graphic [(mutator/setter (fn [^Labeled cell x]
                                  (.setGraphic cell x)
                                  (.put (.getProperties cell) graphics-key x)))
                lifecycle/dynamic]
      ;; definitions
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter]
      :selected-state-callback [:setter lifecycle/scalar])))

;; proxy-super uses reflection because updateItem is protected

(set! *warn-on-reflection* false)

(def lifecycle
  (lifecycle/annotate
    (composite/lifecycle
      {:props props
       :args []
       :ctor (fn []
               (proxy [CheckBoxTreeCell] []
                 (updateItem [item empty]
                   (proxy-super updateItem item empty)
                   (when (not empty)
                     (let [props (.getProperties this)]
                       (when-let [text (.get props text-key)]
                         (proxy-super setText text))
                       (when-let [graphic (.get props graphics-key)]
                         (proxy-super setGraphic graphic)))))))})
    :check-box-tree-cell))
