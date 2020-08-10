(ns cljfx.fx.window
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.jdk.fx.window :as jdk.fx.window]
            [cljfx.mutator :as mutator])
  (:import [javafx.stage Window]
           [javafx.event Event EventHandler]))

(set! *warn-on-reflection* true)

(def props
  (merge
    jdk.fx.window/props
    (composite/props Window
      :event-dispatcher [:setter lifecycle/scalar]
      :event-filter [(mutator/adder-remover
                       #(.addEventFilter ^Window %1 Event/ANY ^EventHandler %2)
                       #(.removeEventFilter ^Window %1 Event/ANY ^EventHandler %2))
                     lifecycle/event-handler
                     :coerce coerce/event-handler]
      :event-handler [(mutator/adder-remover
                        #(.addEventHandler ^Window %1 Event/ANY ^EventHandler %2)
                        #(.removeEventHandler ^Window %1 Event/ANY ^EventHandler %2))
                      lifecycle/event-handler
                      :coerce coerce/event-handler]
      :height [:setter lifecycle/scalar :coerce double :default Double/NaN]
      :on-focused-changed [:property-change-listener lifecycle/change-listener]
      :on-close-request [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :opacity [:setter lifecycle/scalar :coerce double :default 1]
      :user-data [:setter lifecycle/scalar]
      :width [:setter lifecycle/scalar :coerce double :default Double/NaN]
      :x [:setter lifecycle/scalar :coerce double :default Double/NaN]
      :y [:setter lifecycle/scalar :coerce double :default Double/NaN]
      :on-height-changed [:property-change-listener lifecycle/change-listener]
      :on-width-changed [:property-change-listener lifecycle/change-listener]
      :on-x-changed [:property-change-listener lifecycle/change-listener]
      :on-y-changed [:property-change-listener lifecycle/change-listener])))
