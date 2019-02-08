(ns cljfx.fx.line-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape LineTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (lifecycle.composite/props LineTo
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle.composite/describe LineTo
    :ctor []
    :props props))
