(ns cljfx.fx.text-input-control
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TextInputControl]))

(def lifecycle
  (lifecycle.composite/describe TextInputControl
    :extends [fx.control/lifecycle]
    :props {:editable [:setter lifecycle/scalar :default true]
            :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
            :prompt-text [:setter lifecycle/scalar :default ""]
            :text [(mutator/setter (fn [^TextInputControl control text]
                                     (when-not (= text (.getText control))
                                       (.setText control text))))
                   lifecycle/scalar]
            :on-text-changed [:property-change-listener
                              (lifecycle/wrap-coerce lifecycle/event-handler
                                                     coerce/change-listener)]
            :text-formatter [:setter lifecycle/scalar :coerce coerce/text-formatter]}))