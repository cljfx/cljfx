(ns cljfx.fx.tree-table-column
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.table-column-base :as fx.table-column-base]
            [cljfx.fx.tree-table-cell :as fx.tree-table-cell])
  (:import [javafx.scene.control TreeTableColumn TreeTableColumn$SortType
                                 TreeTableColumn$CellDataFeatures]
           [javafx.util Callback]))

(set! *warn-on-reflection* true)

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

(defn cell-factory [x]
  (cond
    (instance? Callback x) x
    (fn? x) (reify Callback
              (call [_ _]
                (fx.tree-table-cell/create x)))
    :else (coerce/fail Callback x)))

(def props
  (merge
    fx.table-column-base/props
    (composite/props TreeTableColumn
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "table-column"]
      ;; definitions
      :cell-factory [:setter (lifecycle/detached-prop-map
                               (:props fx.tree-table-cell/lifecycle))
                     :coerce cell-factory
                     :default TreeTableColumn/DEFAULT_CELL_FACTORY]
      :cell-value-factory [:setter lifecycle/scalar
                           :coerce tree-table-cell-value-factory]
      :columns [:list lifecycle/dynamics]
      :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler] ;; has private default
      :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :sort-type [:setter lifecycle/scalar
                  :coerce (coerce/enum TreeTableColumn$SortType)
                  :default :ascending])))

(def lifecycle
  (composite/describe TreeTableColumn
    :ctor []
    :props props))
