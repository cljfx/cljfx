(ns cljfx.fx.scale
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Scale]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transform/props
    (composite/props Scale
      :pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
      :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
      :pivot-z [:setter lifecycle/scalar :coerce double :default 0.0]
      :x [:setter lifecycle/scalar :coerce double :default 1.0]
      :y [:setter lifecycle/scalar :coerce double :default 1.0]
      :z [:setter lifecycle/scalar :coerce double :default 1.0])))

(def lifecycle
  (composite/describe Scale
    :ctor []
    :props props))
