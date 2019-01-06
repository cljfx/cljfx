(ns cljfx.fx.table-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TableView SelectionMode]))

(def lifecycle
  (lifecycle.composite/describe TableView
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:column-resize-policy [:setter lifecycle/scalar :coerce coerce/table-resize-policy
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
            :sort-policy [:setter lifecycle/scalar :coerce coerce/table-sort-policy
                          :default :default]
            :table-menu-button-visible [:setter lifecycle/scalar :default false]}))