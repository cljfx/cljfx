(ns cljfx.fx.list-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control]
            [cljfx.fx.text-field-list-cell :as fx.text-field-list-cell])
  (:import [javafx.scene.control ListView SelectionMode]
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

(def lifecycle
  (lifecycle.composite/describe ListView
    :ctor []
    :extends [fx.control/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "list-view"]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :list-view]
            ;; definitions
            :cell-factory [:setter (lifecycle/detached-prop-map
                                     (:props fx.text-field-list-cell/lifecycle))
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
            :on-selected-item-changed [(mutator/property-change-listener
                                         #(.selectedItemProperty
                                            (.getSelectionModel ^ListView %)))
                                       (lifecycle/wrap-coerce lifecycle/event-handler
                                                              coerce/change-listener)]
            :selection-mode [(mutator/setter
                               #(.setSelectionMode (.getSelectionModel ^ListView %1) %2))
                             lifecycle/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]}))