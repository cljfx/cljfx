(ns cljfx.fx.h-line-to
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape HLineTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (composite/props HLineTo
      :x [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe HLineTo
    :ctor []
    :props props))
