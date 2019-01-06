(ns cljfx.fx.menu-button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.button-base :as fx.button-base])
  (:import [javafx.scene.control MenuButton]
           [javafx.geometry Side]))

(def lifecycle
  (lifecycle.composite/describe MenuButton
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {:items [:list lifecycle/dynamics]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :popup-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :bottom]}))