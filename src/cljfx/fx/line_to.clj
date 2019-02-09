(ns cljfx.fx.line-to
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape LineTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (composite/props LineTo
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe LineTo
    :ctor []
    :props props))
