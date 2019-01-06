(ns cljfx.fx.toggle-button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.button-base :as fx.button-base])
  (:import [javafx.scene.control ToggleButton]))

(def lifecycle
  (lifecycle.composite/describe ToggleButton
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {:selected [:setter lifecycle/scalar :default false]
            :toggle-group [:setter lifecycle/scalar]}))