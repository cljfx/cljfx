(ns cljfx.fx.polyline
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Polyline]))

(def lifecycle
  (lifecycle.composite/describe Polyline
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:fill [:setter lifecycle/scalar :coerce coerce/paint]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :points [:list lifecycle/scalar :coerce #(map double %)]}))