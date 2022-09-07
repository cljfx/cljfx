(ns cljfx.fx.close-path
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.path-element :as fx.path-element]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.shape ClosePath]))

(set! *warn-on-reflection* true)

(def props
  fx.path-element/props)

(def lifecycle
  (lifecycle/annotate
    (composite/describe ClosePath
      :ctor []
      :props props)
    :close-path))
