(ns cljfx.fx.check-box-tree-cell
  "Part of a public API"
  (:require [cljfx.coerce :as coerce]
            [cljfx.composite :as composite]
            [cljfx.fx.tree-cell :as fx.tree-cell]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.control CheckBox]
           [javafx.scene.control.cell CheckBoxTreeCell]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.tree-cell/props
    (composite/props CheckBoxTreeCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "tree-cell" "check-box-tree-cell"]]
      :text [(mutator/setter (fn [^CheckBoxTreeCell cell x]
                               (.setText cell x)
                               (.put (.getProperties cell) ::text x)))
             lifecycle/scalar
             :default ""]
      :graphic [(mutator/setter (fn [^CheckBoxTreeCell cell x]
                                  (.setGraphic cell x)
                                  (.put (.getProperties cell) ::graphic x)))
                lifecycle/dynamic]
      ;; definitions
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter])))

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
                   (let [^CheckBoxTreeCell this this]
                     (proxy-super updateItem item empty)
                     (when (not empty)
                       (let [props (.getProperties this)]
                         (when-let [text (.get props ::text)]
                           (proxy-super setText text))
                         (when-let [graphic (.get props ::graphic)]
                           (let [current-graphic (.getGraphic this)]
                             (if (instance? CheckBox current-graphic)
                               (.setGraphic ^CheckBox current-graphic graphic)
                               (proxy-super setGraphic graphic))))))))))})
    :check-box-tree-cell))
