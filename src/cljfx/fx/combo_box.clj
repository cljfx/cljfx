(ns cljfx.fx.combo-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.combo-box-base :as fx.combo-box-base]
            [cljfx.fx.text-field-list-cell :as fx.text-field-list-cell])
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


(def lifecycle
  (lifecycle.composite/describe ComboBox
    :ctor []
    :extends [fx.combo-box-base/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["combo-box" "combo-box-base"]]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :combo-box]
            ;; definitions
            :button-cell [:setter (lifecycle/detached-prop-map
                                    (:props fx.text-field-list-cell/lifecycle))
                          :coerce list-cell]
            :cell-factory [:setter (lifecycle/detached-prop-map
                                     (:props fx.text-field-list-cell/lifecycle))
                           :coerce cell-factory]
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                        :default :default]
            :items [:list lifecycle/scalar]
            :placeholder [:setter lifecycle/dynamic]
            :visible-row-count [:setter lifecycle/scalar :coerce int :default 10]}))