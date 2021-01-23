(ns cljfx.fx.choice-dialog
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.dialog :as fx.dialog]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control ChoiceDialog]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.dialog/props
    (composite/props ChoiceDialog
      :items [:list lifecycle/scalar]
      :selected-item [:setter lifecycle/scalar])))

(def lifecycle
  (-> (composite/describe ChoiceDialog
        :ctor []
        :props props
        :prop-order {:showing 1})
      (lifecycle/wrap-on-delete #(.hide ^ChoiceDialog %))))
