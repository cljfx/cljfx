(ns cljfx.fx.sphere
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape3d :as fx.shape3d])
  (:import [javafx.scene.shape Sphere]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape3d/props
    (composite/props Sphere
      :radius [:setter lifecycle/scalar :coerce double :default 1.0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Sphere
      :ctor []
      :props props)
    :sphere))
