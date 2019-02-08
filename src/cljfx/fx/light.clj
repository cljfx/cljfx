(ns cljfx.fx.light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect Light]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props Light
    :color [:setter lifecycle/scalar :coerce coerce/color :default :white]))
