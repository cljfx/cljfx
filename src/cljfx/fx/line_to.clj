(ns cljfx.fx.line-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape LineTo]))

(def lifecycle
  (lifecycle.composite/describe LineTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))