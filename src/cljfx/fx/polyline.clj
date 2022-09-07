(ns cljfx.fx.polyline
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Polyline]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Polyline
      :fill [:setter lifecycle/scalar :coerce coerce/paint]
      :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
      :points [:list lifecycle/scalar :coerce #(map double %)])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Polyline
      :ctor []
      :props props)
    :polyline))
