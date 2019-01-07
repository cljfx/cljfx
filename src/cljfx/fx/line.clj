(ns cljfx.fx.line
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Line]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Line
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:fill [:setter lifecycle/scalar :coerce coerce/paint]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :start-x [:setter lifecycle/scalar :coerce double :default 0]
            :start-y [:setter lifecycle/scalar :coerce double :default 0]
            :end-x [:setter lifecycle/scalar :coerce double :default 0]
            :end-y [:setter lifecycle/scalar :coerce double :default 0]}))