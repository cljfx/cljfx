(ns cljfx.fx.menu-item
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control MenuItem]))

(set! *warn-on-reflection* true)

(def props
  (composite/props MenuItem
    :accelerator [:setter lifecycle/scalar :coerce coerce/key-combination]
    :disable [:setter lifecycle/scalar :default false]
    :graphic [:setter lifecycle/dynamic]
    :id [:setter lifecycle/scalar]
    :mnemonic-parsing [:setter lifecycle/scalar :default true]
    :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-menu-validation [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
    :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "menu-item"]
    :text [:setter lifecycle/scalar]
    :user-data [:setter lifecycle/scalar]
    :visible [:setter lifecycle/scalar :default true]))

(def lifecycle
  (composite/describe MenuItem
    :ctor []
    :props props))
