(ns cljfx.fx.scale
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Scale]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Scale
    :ctor []
    :extends [fx.transform/lifecycle]
    :props {:pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-z [:setter lifecycle/scalar :coerce double :default 0.0]
            :x [:setter lifecycle/scalar :coerce double :default 1.0]
            :y [:setter lifecycle/scalar :coerce double :default 1.0]
            :z [:setter lifecycle/scalar :coerce double :default 1.0]}))