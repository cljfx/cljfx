(ns cljfx.fx.choice-box
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ChoiceBox]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (composite/props ChoiceBox
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "choice-box"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :combo-box]
      ;; definitions
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter]
      :items [:list lifecycle/scalar]
      :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :value [:setter lifecycle/scalar]
      :on-value-changed [:property-change-listener lifecycle/change-listener])))

(def lifecycle
  (composite/describe ChoiceBox
    :ctor []
    :props props))
