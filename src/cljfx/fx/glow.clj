(ns cljfx.fx.glow
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Glow]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Glow
    :input [:setter lifecycle/dynamic]
    :level [:setter lifecycle/scalar :coerce double :default 0.3]))

(def lifecycle
  (composite/describe Glow
    :ctor []
    :props props))
