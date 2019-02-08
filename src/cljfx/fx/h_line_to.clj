(ns cljfx.fx.h-line-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape HLineTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (lifecycle.composite/props HLineTo
      :x [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle.composite/describe HLineTo
    :ctor []
    :props props))
