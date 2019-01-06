(ns cljfx.fx.check-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.button-base :as fx.button-base])
  (:import [javafx.scene.control CheckBox]))

(def lifecycle
  (lifecycle.composite/describe CheckBox
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {:allow-indeterminate [:setter lifecycle/scalar :default false]
            :indeterminate [:setter lifecycle/scalar :default false]
            :selected [:setter lifecycle/scalar :default false]
            :on-selected-changed [:property-change-listener
                                  (lifecycle/wrap-coerce lifecycle/event-handler
                                                         coerce/change-listener)]}))