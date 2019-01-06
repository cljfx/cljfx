(ns cljfx.fx.tab
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Tab]))

(def lifecycle
  (lifecycle.composite/describe Tab
    :ctor []
    :props {:closable [:setter lifecycle/scalar :default true]
            :content [:setter lifecycle/dynamic]
            :context-menu [:setter lifecycle/dynamic]
            :disable [:setter lifecycle/scalar :default false]
            :graphic [:setter lifecycle/dynamic]
            :id [:setter lifecycle/scalar]
            :on-close-request [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-closed [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-selection-changed [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :style [:setter lifecycle/scalar :coerce coerce/style]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]
            :text [:setter lifecycle/scalar]
            :tooltip [:setter lifecycle/dynamic]
            :user-data [:setter lifecycle/scalar]}))