(ns cljfx.fx.pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.region :as fx.region]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.layout Pane]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.region/props
    (lifecycle.composite/props Pane
      :children [:list lifecycle/dynamics])))

(def lifecycle
  (lifecycle.composite/describe Pane
    :ctor []
    :props props))
