(ns cljfx.fx.distant-light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.light :as fx.light])
  (:import [javafx.scene.effect Light$Distant]))

(def lifecycle
  (lifecycle.composite/describe Light$Distant
    :ctor []
    :extends [fx.light/lifecycle]
    :props {:azimuth [:setter lifecycle/scalar :coerce double :default 45]
            :elevation [:setter lifecycle/scalar :coerce double :default 45]}))