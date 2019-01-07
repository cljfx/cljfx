(ns cljfx.fx.number-axis
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.value-axis :as fx.value-axis])
  (:import [javafx.scene.chart NumberAxis]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe NumberAxis
    :ctor []
    :extends [fx.value-axis/lifecycle]
    :props {:force-zero-in-range [:setter lifecycle/scalar :default true]
            :tick-unit [:setter lifecycle/scalar :coerce double :default 5.0]}))