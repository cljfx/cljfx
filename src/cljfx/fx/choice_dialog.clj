(ns cljfx.fx.choice-dialog
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.dialog :as fx.dialog]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control ChoiceDialog]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe ChoiceDialog
    :ctor []
    :extends [fx.dialog/lifecycle]
    :props {:items [:list lifecycle/scalar]
            :selected-item [:setter lifecycle/scalar]}))
