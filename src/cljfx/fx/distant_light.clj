(ns cljfx.fx.distant-light
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.light :as fx.light])
  (:import [javafx.scene.effect Light$Distant]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.light/props
    (composite/props Light$Distant
      :azimuth [:setter lifecycle/scalar :coerce double :default 45]
      :elevation [:setter lifecycle/scalar :coerce double :default 45])))

(def lifecycle
  (composite/describe Light$Distant
    :ctor []
    :props props))
