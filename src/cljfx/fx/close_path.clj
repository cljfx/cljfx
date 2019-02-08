(ns cljfx.fx.close-path
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape ClosePath]))

(set! *warn-on-reflection* true)

(def props
  fx.path-element/props)

(def lifecycle
  (lifecycle.composite/describe ClosePath
    :ctor []
    :props props))
