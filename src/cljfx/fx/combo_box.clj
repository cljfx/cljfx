(ns cljfx.fx.combo-box
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.combo-box-base :as fx.combo-box-base]
            [cljfx.fx.text-field-list-cell :as fx.text-field-list-cell]
            [cljfx.ext.cell-factory :as ext.cell-factory])
  (:import [javafx.scene.control ComboBox ListCell]
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

(defn list-cell [x]
  (cond
    (instance? ListCell x) x
    (fn? x) (fx.text-field-list-cell/create x)
    :else (coerce/fail ListCell x)))

(def props
  (merge
    fx.combo-box-base/props
    (composite/props ComboBox
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["combo-box" "combo-box-base"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :combo-box]
      ;; definitions
      :button-cell [:setter (lifecycle/detached-prop-map fx.text-field-list-cell/props)
                    :coerce list-cell]
      :cell-factory [:setter (lifecycle/if-desc map?
                               ext.cell-factory/lifecycle
                               (lifecycle/detached-prop-map fx.text-field-list-cell/props))
                     :coerce cell-factory]
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                  :default :default]
      :items [:list lifecycle/scalar]
      :placeholder [:setter lifecycle/dynamic]
      :visible-row-count [:setter lifecycle/scalar :coerce int :default 10])))

(def lifecycle
  (composite/describe ComboBox
    :ctor []
    :props props))
