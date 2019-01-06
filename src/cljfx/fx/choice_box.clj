(ns cljfx.fx.choice-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ChoiceBox]))

(def lifecycle
  (lifecycle.composite/describe ChoiceBox
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:converter [:setter lifecycle/scalar :coerce coerce/string-converter]
            :items [:list lifecycle/scalar]
            :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :value [:setter lifecycle/scalar]}))