(ns cljfx.fx.table-column
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.table-column-base :as fx.table-column-base]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TableColumn TableColumn$SortType TableColumn$CellDataFeatures]
           [javafx.util Callback]))

(defn table-cell-value-factory [x]
  (cond
    (instance? Callback x) x
    (fn? x) (reify Callback
              (call [_ param]
                (let [^TableColumn$CellDataFeatures features param]
                  (coerce/constant-observable-value (x (.getValue features))))))
    :else (coerce/fail Callback x)))

(def lifecycle
  (lifecycle.composite/describe TableColumn
    :ctor []
    :extends [fx.table-column-base/lifecycle]
    :props {:cell-factory [:setter lifecycle/scalar :default TableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter lifecycle/scalar :coerce table-cell-value-factory]
            :columns [:list lifecycle/dynamics]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :sort-type [:setter lifecycle/scalar
                        :coerce (coerce/enum TableColumn$SortType)
                        :default :ascending]}))