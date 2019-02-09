(ns cljfx.fx.tree-item
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TreeItem]))

(set! *warn-on-reflection* true)

(def props
  (composite/props TreeItem
    :children [:list lifecycle/dynamics]
    :expanded [:setter lifecycle/scalar :default false]
    :graphic [:setter lifecycle/dynamic]
    :value [:setter lifecycle/scalar]))

(def lifecycle
  (composite/describe TreeItem
    :ctor []
    :props props))
