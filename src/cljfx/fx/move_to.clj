(ns cljfx.fx.move-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape MoveTo]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe MoveTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))