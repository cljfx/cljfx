(ns cljfx.fx.table-view
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control]
            [cljfx.ext.selection-model :as ext.selection-model]
            [cljfx.ext.multiple-selection-model :as ext.multiple-selection-model]
            [cljfx.ext.cell-factory :as ext.cell-factory])
  (:import [javafx.scene.control TableView]
           [javafx.util Callback]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(defn- table-resize-policy [x]
  (cond
    (instance? Callback x) x
    (= :unconstrained x) TableView/UNCONSTRAINED_RESIZE_POLICY
    (= :constrained x) TableView/CONSTRAINED_RESIZE_POLICY
    (fn? x) (reify Callback
              (call [_ param]
                (x param)))
    :else (coerce/fail Callback x)))

(defn- table-sort-policy [x]
  (cond
    (instance? Callback x) x
    (= :default x) TableView/DEFAULT_SORT_POLICY
    :else (coerce/fail Callback x)))

(defn- get-selection-model [^TableView %]
  (.getSelectionModel %))

(def props
  (merge
    fx.control/props
    (composite/props TableView
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "table-view"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :table-view]
      ;; definitions
      :column-resize-policy [:setter lifecycle/scalar :coerce table-resize-policy
                             :default :unconstrained]
      :columns [:list lifecycle/dynamics]
      :editable [:setter lifecycle/scalar :default false]
      :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
      :items [:list lifecycle/scalar]
      :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-scroll-to-column [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-sort [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :placeholder [:setter lifecycle/dynamic]
      :row-factory [:setter (lifecycle/if-desc map?
                              ext.cell-factory/lifecycle
                              lifecycle/scalar)]
      ;; deprecated, use [[cljfx.ext.table-view/with-selection-props]] instead
      :on-selected-item-changed (ext.selection-model/on-selected-item-changed-prop
                                  get-selection-model)
      ;; deprecated, use [[cljfx.ext.table-view/with-selection-props]] instead
      :selection-mode (ext.multiple-selection-model/selection-mode-prop
                        get-selection-model
                        :single)
      :sort-order [:list lifecycle/dynamics]
      :sort-policy [:setter lifecycle/scalar :coerce table-sort-policy
                    :default :default]
      :table-menu-button-visible [:setter lifecycle/scalar :default false])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe TableView
      :ctor []
      :props props)
    :table-view))
