(ns cljfx.jdk.fx.menu-button
  (:require [cljfx.composite :as composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control MenuButton]))

(set! *warn-on-reflection* true)

(def props
  (composite/props MenuButton
    :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]))