(ns cljfx.fx.tree-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TreeView SelectionMode]))

(def lifecycle
  (lifecycle.composite/describe TreeView
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:cell-factory [:setter lifecycle/scalar]
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
            :show-root [:setter lifecycle/scalar :default true]}))