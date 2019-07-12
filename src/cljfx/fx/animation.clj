(ns cljfx.fx.animation
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator])
  (:import [javafx.animation Animation]
           [javafx.util Duration]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Animation
    :auto-reverse [:setter lifecycle/scalar :default false]
    :on-auto-reverse-changed [:property-change-listener lifecycle/change-listener]
    :on-current-time-changed [:property-change-listener lifecycle/change-listener]
    :cycle-count [:setter lifecycle/scalar :coerce coerce/animation :default 1.0]
    :on-cycle-count-changed [:property-change-listener lifecycle/change-listener]
    :on-cycle-duration-changed [:property-change-listener lifecycle/change-listener]
    :delay [:setter lifecycle/scalar :coerce coerce/duration :default 0]
    :on-delay-changed [:property-change-listener lifecycle/change-listener]
    :on-finished [:setter lifecycle/event-handler :coerce coerce/event-handler :default nil]
    :on-on-finished-changed [:property-change-listener lifecycle/change-listener]
    :rate [:setter lifecycle/scalar :coerce double :default 1.0]
    :on-rate-changed [:property-change-listener lifecycle/change-listener]
    :jump-to [(mutator/setter
                #(if (string? %2)
                   (.jumpTo ^Animation %1 ^String %2)
                   (.jumpTo ^Animation %1 ^Duration %2)))
              lifecycle/scalar
              :coerce #(if (string? %)
                         %
                         (coerce/duration %))]
    :on-status-changed [:property-change-listener lifecycle/change-listener]
    :status [(mutator/setter
               #(case %2
                  :running (.play ^Animation %1)
                  :paused (.pause ^Animation %1)
                  :stopped (.stop ^Animation %1)))
             lifecycle/scalar
             :default :stopped]))
