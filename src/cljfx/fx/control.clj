(ns cljfx.fx.control
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.region :as fx.region])
  (:import [javafx.scene.control Control]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Control
    :extends [fx.region/lifecycle]
    :props {:context-menu [:setter lifecycle/dynamic]
            :tooltip [:setter lifecycle/dynamic]}))
