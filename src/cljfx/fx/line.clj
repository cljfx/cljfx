(ns cljfx.fx.line
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Line]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Line
      :fill [:setter lifecycle/scalar :coerce coerce/paint]
      :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
      :start-x [:setter lifecycle/scalar :coerce double :default 0]
      :start-y [:setter lifecycle/scalar :coerce double :default 0]
      :end-x [:setter lifecycle/scalar :coerce double :default 0]
      :end-y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (composite/describe Line
    :ctor []
    :props props))
