(ns cljfx.fx.tree-view
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.tree-cell :as fx.tree-cell]
            [cljfx.fx.control :as fx.control]
            [cljfx.ext.selection-model :as ext.selection-model]
            [cljfx.ext.multiple-selection-model :as ext.multiple-selection-model])
  (:import [javafx.scene.control TreeView MultipleSelectionModel]
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

(defn- get-selection-model ^MultipleSelectionModel [^TreeView view]
  (.getSelectionModel view))

(def props
  (merge
    fx.control/props
    (composite/props TreeView
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
      ;; deprecated, use [[cljfx.ext.tree-view/with-selection-props]] instead
      :on-selected-item-changed (ext.selection-model/on-selected-item-changed-prop
                                  get-selection-model)
      ;; deprecated, use [[cljfx.ext.tree-view/with-selection-props]] instead
      :selection-mode (ext.multiple-selection-model/selection-mode-prop
                        get-selection-model
                        :single)
      :show-root [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe TreeView
    :ctor []
    :props props))
