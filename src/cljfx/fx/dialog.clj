(ns cljfx.fx.dialog
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.control Dialog]
           [javafx.util Callback]
           [javafx.stage Modality StageStyle]))

(set! *warn-on-reflection* true)

(defn result-converter [x]
  (cond
    (instance? Callback x)
    x

    (fn? x)
    (reify Callback
      (call [_ p]
        (x p)))

    :else
    (coerce/fail Callback x)))

(def props
  (composite/props Dialog
    :content-text [:setter lifecycle/scalar]
    :dialog-pane [:setter lifecycle/dynamic]
    :graphic [:setter lifecycle/dynamic]
    :header-text [:setter lifecycle/scalar]
    :height [:setter lifecycle/scalar :coerce double :default ##NaN]
    :modality [(mutator/setter #(.initModality ^Dialog %1 %2)) lifecycle/scalar
               :coerce (coerce/enum Modality) :default :application-modal]
    :on-close-request [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :owner [(mutator/setter #(.initOwner ^Dialog %1 %2)) lifecycle/dynamic]
    :resizable [:setter lifecycle/scalar :default false]
    :result [:setter lifecycle/scalar]
    :result-converter [:setter lifecycle/scalar :coerce result-converter]
    :showing [(mutator/setter #(if %2 (.show ^Dialog %1) (.hide ^Dialog %1)))
              lifecycle/scalar
              :default false]
    :style [(mutator/setter #(.initStyle ^Dialog %1 %2)) lifecycle/scalar
            :coerce (coerce/enum StageStyle) :default :decorated]
    :title [:setter lifecycle/scalar]
    :width [:setter lifecycle/scalar :coerce double :default ##NaN]
    :x [:setter lifecycle/scalar :coerce double :default ##NaN]
    :y [:setter lifecycle/scalar :coerce double :default ##NaN]))

(def lifecycle
  (-> (composite/describe Dialog
        :ctor []
        :prop-order {:showing 1}
        :props props)
      (lifecycle/wrap-on-delete #(.hide ^Dialog %))))
