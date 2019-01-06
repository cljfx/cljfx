(ns cljfx.fx.tree-table-column
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.table-column-base :as fx.table-column-base])
  (:import [javafx.scene.control TreeTableColumn TreeTableColumn$SortType]))

(def lifecycle
  (lifecycle.composite/describe TreeTableColumn
    :ctor []
    :extends [fx.table-column-base/lifecycle]
    :props {:cell-factory [:setter lifecycle/scalar
                           :default TreeTableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter lifecycle/scalar
                                 :coerce coerce/tree-table-cell-value-factory]
            :columns [:list lifecycle/dynamics]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :sort-type [:setter lifecycle/scalar
                        :coerce (coerce/enum TreeTableColumn$SortType)
                        :default :ascending]}))