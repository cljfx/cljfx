(ns cljfx.fx.translate
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Translate]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Translate
    :ctor []
    :extends [fx.transform/lifecycle]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]
            :z [:setter lifecycle/scalar :coerce double :default 0.0]}))