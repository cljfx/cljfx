(ns cljfx.fx.combo-box-base
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ComboBoxBase]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (composite/props ComboBoxBase
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "combo-box-base"]
      ;; definitions
      :editable [:setter lifecycle/scalar :default false]
      :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :prompt-text [:setter lifecycle/scalar]
      :value [:setter lifecycle/scalar]
      :on-value-changed [:property-change-listener lifecycle/change-listener])))
