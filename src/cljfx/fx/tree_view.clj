(ns cljfx.fx.tree-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.tree-cell :as fx.tree-cell]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TreeView SelectionMode]
           [javafx.scene AccessibleRole]
           [javafx.util Callback]))

(set! *warn-on-reflection* true)

(defn cell-factory [x]
  (cond
    (instance? Callback x) x
    (fn? x) (reify Callback
              (call [_ _]
                (fx.tree-cell/create x)))
    :else (coerce/fail Callback x)))

(def props
  (merge
    fx.control/props
    (lifecycle.composite/props TreeView
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "tree-view"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tree-view]
      ;; definitions
      :cell-factory [:setter (lifecycle/detached-prop-map fx.tree-cell/props)
                     :coerce cell-factory]
      :editable [:setter lifecycle/scalar :default false]
      :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
      :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :root [:setter lifecycle/dynamic]
      :on-selected-item-changed [(mutator/property-change-listener
                                   #(.selectedItemProperty
                                      (.getSelectionModel ^TreeView %)))
                                 (lifecycle/wrap-coerce lifecycle/event-handler
                                                        coerce/change-listener)]
      :selection-mode [(mutator/setter
                         #(.setSelectionMode (.getSelectionModel ^TreeView %1) %2))
                       lifecycle/scalar
                       :coerce (coerce/enum SelectionMode)
                       :default :single]
      :show-root [:setter lifecycle/scalar :default true])))

(def lifecycle
  (lifecycle.composite/describe TreeView
    :ctor []
    :props props))
