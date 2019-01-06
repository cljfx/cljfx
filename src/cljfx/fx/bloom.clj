(ns cljfx.fx.bloom
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Bloom]))

(def lifecycle
  (lifecycle.composite/describe Bloom
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :threshold [:setter lifecycle/scalar :coerce double :default 0.3]}))