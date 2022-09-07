(ns cljfx.fx.toggle-group
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control ToggleGroup]))

(set! *warn-on-reflection* true)

(def props
  (composite/props ToggleGroup
    :toggles [:list lifecycle/dynamics]
    :user-data [:setter lifecycle/scalar]))

(def lifecycle
  (lifecycle/annotate
    (composite/describe ToggleGroup
      :ctor []
      :props props)
    :toggle-group))
