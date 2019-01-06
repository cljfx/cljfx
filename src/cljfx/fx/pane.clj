(ns cljfx.fx.pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.region :as fx.region]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.layout Pane]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Pane
    :ctor []
    :extends [fx.region/lifecycle]
    :props {:children [:list lifecycle/dynamics]}))
