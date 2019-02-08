(ns cljfx.fx.move-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape MoveTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (lifecycle.composite/props MoveTo
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle.composite/describe MoveTo
    :ctor []
    :props props))
