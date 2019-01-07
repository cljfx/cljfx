(ns cljfx.fx.reflection
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Reflection]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Reflection
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :top-offset [:setter lifecycle/scalar :coerce double :default 0.0]
            :fraction [:setter lifecycle/scalar :coerce double :default 0.75]
            :top-opacity [:setter lifecycle/scalar :coerce double :default 0.5]
            :bottom-opacity [:setter lifecycle/scalar :coerce double :default 0.0]}))