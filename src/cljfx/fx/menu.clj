(ns cljfx.fx.menu
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.menu-item :as fx.menu-item])
  (:import [javafx.scene.control Menu]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.menu-item/props
    (composite/props Menu
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["menu" "menu-item"]]
      ;; definitions
      :items [:list lifecycle/dynamics]
      :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler])))

(def lifecycle
  (composite/describe Menu
    :ctor []
    :props props))
