(ns cljfx.fx.table-view
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TableView SelectionMode]
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
      :row-factory [:setter lifecycle/scalar]
      :on-selected-item-changed [(mutator/property-change-listener
                                   #(.selectedItemProperty
                                      (.getSelectionModel ^TableView %)))
                                 (lifecycle/wrap-coerce lifecycle/event-handler
                                                        coerce/change-listener)]
      :selection-mode [(mutator/setter
                         #(.setSelectionMode (.getSelectionModel ^TableView %1) %2))
                       lifecycle/scalar
                       :coerce (coerce/enum SelectionMode)
                       :default :single]
      ; :sort-order [:list] ;; should be list of refs to columns
      :sort-policy [:setter lifecycle/scalar :coerce table-sort-policy
                    :default :default]
      :table-menu-button-visible [:setter lifecycle/scalar :default false])))

(def lifecycle
  (composite/describe TableView
    :ctor []
    :props props))
