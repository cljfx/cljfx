(ns cljfx.fx.tree-item
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TreeItem]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props TreeItem
    :children [:list lifecycle/dynamics]
    :expanded [:setter lifecycle/scalar :default false]
    :graphic [:setter lifecycle/dynamic]
    :value [:setter lifecycle/scalar]))

(def lifecycle
  (lifecycle.composite/describe TreeItem
    :ctor []
    :props props))
