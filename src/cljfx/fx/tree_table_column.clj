(ns cljfx.fx.tree-table-column
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.table-column-base :as fx.table-column-base])
  (:import [javafx.scene.control TreeTableColumn TreeTableColumn$SortType
                                 TreeTableColumn$CellDataFeatures]
           [javafx.util Callback]))

(defn- tree-table-cell-value-factory [x]
  (cond
    (instance? Callback x)
    x

    (fn? x)
    (reify Callback
      (call [_ param]
        (let [^TreeTableColumn$CellDataFeatures features param]
          (coerce/constant-observable-value (x (.getValue (.getValue features)))))))

    :else
    (coerce/fail Callback x)))

(def lifecycle
  (lifecycle.composite/describe TreeTableColumn
    :ctor []
    :extends [fx.table-column-base/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "table-column"]
            ;; definitions
            :cell-factory [:setter lifecycle/scalar
                           :default TreeTableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter lifecycle/scalar
                                 :coerce tree-table-cell-value-factory]
            :columns [:list lifecycle/dynamics]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :sort-type [:setter lifecycle/scalar
                        :coerce (coerce/enum TreeTableColumn$SortType)
                        :default :ascending]}))