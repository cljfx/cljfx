(ns cljfx.fx.h-line-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape HLineTo]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe HLineTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]}))