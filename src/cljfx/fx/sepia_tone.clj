(ns cljfx.fx.sepia-tone
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect SepiaTone]))

(def lifecycle
  (lifecycle.composite/describe SepiaTone
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :level [:setter lifecycle/scalar :coerce double :default 1.0]}))