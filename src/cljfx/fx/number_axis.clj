(ns cljfx.fx.number-axis
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.value-axis :as fx.value-axis])
  (:import [javafx.scene.chart NumberAxis]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.value-axis/props
    (composite/props NumberAxis
      :force-zero-in-range [:setter lifecycle/scalar :default true]
      :tick-unit [:setter lifecycle/scalar :coerce double :default 5.0])))

(def lifecycle
  (composite/describe NumberAxis
    :ctor []
    :props props))
