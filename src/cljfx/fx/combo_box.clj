(ns cljfx.fx.combo-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.combo-box-base :as fx.combo-box-base]
            [cljfx.fx.text-field-list-cell :as fx.text-field-list-cell])
  (:import [javafx.scene.control ComboBox ListCell]
           [javafx.scene AccessibleRole]
           [javafx.util Callback]
           [javafx.scene.control.cell TextFieldListCell]))

(set! *warn-on-reflection* true)

(defn- create-cell [f]
  (let [*props (volatile! {})]
    (proxy [TextFieldListCell] []
      (updateItem [item empty]
        (let [^TextFieldListCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (f (select-keys props [:text :graphic]) this {} empty)
          (vreset! *props (f (dissoc props :text :graphic) this item empty)))))))

(defn cell-factory [x]
  (cond
    (instance? Callback x) x
    (fn? x) (reify Callback
              (call [_ _]
                (create-cell x)))
    :else (coerce/fail Callback x)))

(defn list-cell [x]
  (cond
    (instance? ListCell x) x
    (fn? x) (create-cell x)
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