(ns cljfx.fx.cylinder
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape Cylinder]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape3d/props
    (composite/props Cylinder
      :height [:setter lifecycle/scalar :coerce double :default 2.0]
      :radius [:setter lifecycle/scalar :coerce double :default 1.0])))

(def lifecycle
  (composite/describe Cylinder
    :ctor []
    :props props))
