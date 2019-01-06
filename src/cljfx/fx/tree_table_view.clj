(ns cljfx.fx.tree-table-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TreeTableView SelectionMode TreeSortMode]
           [javafx.util Callback]
           [javafx.scene AccessibleRole]))

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

(def lifecycle
  (lifecycle.composite/describe TreeTableView
    :ctor []
    :extends [fx.control/lifecycle]
    :props {;; overrides
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
            :on-selected-item-changed [(mutator/property-change-listener
                                         #(.selectedItemProperty
                                            (.getSelectionModel ^TreeTableView %)))
                                       (lifecycle/wrap-coerce lifecycle/event-handler
                                                              coerce/change-listener)]
            :selection-mode [(mutator/setter
                               #(.setSelectionMode (.getSelectionModel ^TreeTableView %1) %2))
                             lifecycle/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            :show-root [:setter lifecycle/scalar :default true]
            :sort-mode [:setter lifecycle/scalar :coerce (coerce/enum TreeSortMode)
                        :default :all-descendants]
            ; :sort-order [:list] ;; should be list of refs to columns
            :sort-policy [:setter lifecycle/scalar :coerce tree-table-sort-policy
                          :default :default]
            ; :tree-column [:setter lifecycle/many-dynamic-hiccups] ;; should be a ref to column
            :table-menu-button-visible [:setter lifecycle/scalar :default false]}))