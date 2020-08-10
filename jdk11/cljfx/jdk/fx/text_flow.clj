(ns cljfx.jdk.fx.text-flow
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.text TextFlow]))

(set! *warn-on-reflection* true)

(def props
  (composite/props TextFlow
    :tab-size [:setter lifecycle/scalar :coerce int :default 8]))
