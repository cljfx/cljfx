(ns cljfx.fx.tree-table-view
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control]
            [cljfx.ext.selection-model :as ext.selection-model]
            [cljfx.ext.multiple-selection-model :as ext.multiple-selection-model])
  (:import [javafx.scene.control TreeTableView TreeSortMode MultipleSelectionModel]
           [javafx.util Callback]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(defn- tree-table-resize-policy [x]
  (cond
    (instance? Callback x) x
    (= :unconstrained x) TreeTableView/UNCONSTRAINED_RESIZE_POLICY
    (= :constrained x) TreeTableView/CONSTRAINED_RESIZE_POLICY
    :else (coerce/fail Callback "x")))

(defn- tree-table-sort-policy [x]
  (cond
    (instance? Callback x) x
    (= :default x) TreeTableView/DEFAULT_SORT_POLICY
    :else (coerce/fail Callback x)))

(defn- get-selection-model ^MultipleSelectionModel [^TreeTableView tree-table-view]
  (.getSelectionModel tree-table-view))

(def props
  (merge
    fx.control/props
    (composite/props TreeTableView
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "tree-table-view"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tree-table-view]
      ;; definitions
      :column-resize-policy [:setter lifecycle/scalar
                             :coerce tree-table-resize-policy
                             :default :unconstrained]
      :columns [:list lifecycle/dynamics]
      :editable [:setter lifecycle/scalar :default false]
      :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
      :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-scroll-to-column [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-sort [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :placeholder [:setter lifecycle/dynamic]
      :root [:setter lifecycle/dynamic]
      :row-factory [:setter lifecycle/scalar]
      ;; deprecated, use [[cljfx.ext.tree-table-view/with-selection-props]] instead
      :on-selected-item-changed (ext.selection-model/on-selected-item-changed-prop
                                  get-selection-model)
      ;; deprecated, use [[cljfx.ext.tree-table-view/with-selection-props]] instead
      :selection-mode (ext.multiple-selection-model/selection-mode-prop
                        get-selection-model
                        :single)
      :show-root [:setter lifecycle/scalar :default true]
      :sort-mode [:setter lifecycle/scalar :coerce (coerce/enum TreeSortMode)
                  :default :all-descendants]
      :sort-order [:list lifecycle/dynamics]
      :sort-policy [:setter lifecycle/scalar :coerce tree-table-sort-policy
                    :default :default]
      :tree-column [:setter lifecycle/dynamic]
      :table-menu-button-visible [:setter lifecycle/scalar :default false])))

(def lifecycle
  (composite/describe TreeTableView
    :ctor []
    :props props))
