(ns cljfx.fx.path-element
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.shape PathElement]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props PathElement
    :absolute [:setter lifecycle/scalar :default true]))
