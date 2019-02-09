(ns cljfx.fx.light
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect Light]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Light
    :color [:setter lifecycle/scalar :coerce coerce/color :default :white]))
