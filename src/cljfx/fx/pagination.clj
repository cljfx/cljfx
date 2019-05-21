(ns cljfx.fx.pagination
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Pagination]
           [javafx.util Callback]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(defn- page-factory [x]
  (cond
    (instance? Callback x)
    x

    (fn? x)
    (reify Callback
      (call [_ param]
        (x param)))

    :else
    (coerce/fail Callback x)))

(def props
  (merge
    fx.control/props
    (composite/props Pagination
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "pagination"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :pagination]
      ;; definitions
      :current-page-index [:setter lifecycle/scalar :coerce int :default 0]
      :on-current-page-index-changed [:property-change-listener lifecycle/change-listener]
      :max-page-indicator-count [:setter lifecycle/scalar :coerce int :default 10]
      :page-count [:setter lifecycle/scalar :coerce int :default Integer/MAX_VALUE]
      :page-factory [:setter (lifecycle/wrap-factory lifecycle/dynamic)
                     :coerce page-factory])))

(def lifecycle
  (composite/describe Pagination
    :ctor []
    :props props))
