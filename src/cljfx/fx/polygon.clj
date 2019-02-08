(ns cljfx.fx.polygon
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Polygon]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (lifecycle.composite/props Polygon
      :points [:list lifecycle/scalar :coerce #(map double %)])))

(def lifecycle
  (lifecycle.composite/describe Polygon
    :ctor []
    :props props))
