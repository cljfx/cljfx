(ns cljfx.fx.control
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.region :as fx.region])
  (:import [javafx.scene.control Control]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.region/props
    (composite/props Control
      :context-menu [:setter lifecycle/dynamic]
      :tooltip [:setter lifecycle/dynamic])))

