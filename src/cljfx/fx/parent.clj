(ns cljfx.fx.parent
  (:require [cljfx.composite :as composite]
            [cljfx.fx.node :as fx.node]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene Parent]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (composite/props Parent
      :stylesheets [:list lifecycle/scalar :default []])))
