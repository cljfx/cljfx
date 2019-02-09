(ns cljfx.fx.polygon
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Polygon]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Polygon
      :points [:list lifecycle/scalar :coerce #(map double %)])))

(def lifecycle
  (composite/describe Polygon
    :ctor []
    :props props))
