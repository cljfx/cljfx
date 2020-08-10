(ns cljfx.fx.list-view
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control]
            [cljfx.fx.text-field-list-cell :as fx.text-field-list-cell]
            [cljfx.ext.cell-factory :as ext.cell-factory]
            [cljfx.ext.selection-model :as ext.selection-model]
            [cljfx.ext.multiple-selection-model :as ext.multiple-selection-model])
  (:import [javafx.scene.control ListView SelectionModel]
           [javafx.geometry Orientation]
           [javafx.scene AccessibleRole]
           [javafx.util Callback]))

(set! *warn-on-reflection* true)

(defn cell-factory [x]
  (cond
    (instance? Callback x) x
    (fn? x) (reify Callback
              (call [_ _]
                (fx.text-field-list-cell/create x)))
    :else (coerce/fail Callback x)))

(defn- get-selection-model ^SelectionModel [^ListView view]
  (.getSelectionModel view))

(def props
  (merge
    fx.control/props
    (composite/props ListView
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "list-view"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :list-view]
      ;; definitions
      :cell-factory [:setter (lifecycle/if-desc map?
                               ext.cell-factory/lifecycle
                               (lifecycle/detached-prop-map fx.text-field-list-cell/props))
                     :coerce cell-factory]
      :editable [:setter lifecycle/scalar :default false]
      :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
      :items [:list lifecycle/scalar]
      :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                    :default :vertical]
      :placeholder [:setter lifecycle/dynamic]
      ;; deprecated, use [[cljfx.ext.list-view/with-selection-props]] instead
      :on-selected-item-changed (ext.selection-model/on-selected-item-changed-prop
                                  get-selection-model)
      ;; deprecated, use [[cljfx.ext.list-view/with-selection-props]] instead
      :selection-mode (ext.multiple-selection-model/selection-mode-prop
                        get-selection-model
                        :single))))

(def lifecycle
  (composite/describe ListView
    :ctor []
    :props props))
