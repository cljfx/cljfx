(ns cljfx.fx.pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.region :as fx.region]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.layout Pane]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.region/props
    (composite/props Pane
      :children [:list lifecycle/dynamics])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Pane
      :ctor []
      :props props)
    :pane))
