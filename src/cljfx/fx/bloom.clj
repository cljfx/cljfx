(ns cljfx.fx.bloom
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Bloom]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Bloom
    :input [:setter lifecycle/dynamic]
    :threshold [:setter lifecycle/scalar :coerce double :default 0.3]))

(def lifecycle
  (composite/describe Bloom
    :ctor []
    :props props))
