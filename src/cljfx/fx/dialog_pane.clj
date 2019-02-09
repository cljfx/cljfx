(ns cljfx.fx.dialog-pane
  (:require [cljfx.composite :as composite]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control DialogPane ButtonType ButtonBar$ButtonData]))

(set! *warn-on-reflection* true)

(defn button-type [x]
  (cond
    (instance? ButtonType x) x
    (instance? String x) (ButtonType. x)
    (map? x) (ButtonType. (:text x) (coerce/enum ButtonBar$ButtonData (:button-data x)))
    :else (case x
            :apply ButtonType/APPLY
            :cancel ButtonType/CANCEL
            :close ButtonType/CLOSE
            :finish ButtonType/FINISH
            :next ButtonType/NEXT
            :no ButtonType/NO
            :ok ButtonType/OK
            :previous ButtonType/PREVIOUS
            :yes ButtonType/YES
            (coerce/fail ButtonType x))))

(def props
  (merge
    fx.pane/props
    (composite/props DialogPane
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "dialog-pane"]
      ;; definitions
      :button-types [:list
                     (lifecycle/wrap-many lifecycle/scalar
                                          (constantly nil)
                                          identity)
                     :coerce #(map button-type %)]
      :content [:setter lifecycle/dynamic]
      :content-text [:setter lifecycle/scalar]
      :expandable-content [:setter lifecycle/dynamic]
      :expanded [:setter lifecycle/scalar :default false]
      :graphic [:setter lifecycle/dynamic]
      :header [:setter lifecycle/dynamic]
      :header-text [:setter lifecycle/scalar])))

(def lifecycle
  (composite/describe DialogPane
    :ctor []
    :props props))
