(ns cljfx.fx.bloom
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Bloom]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props Bloom
    :input [:setter lifecycle/dynamic]
    :threshold [:setter lifecycle/scalar :coerce double :default 0.3]))

(def lifecycle
  (lifecycle.composite/describe Bloom
    :ctor []
    :props props))
