(ns cljfx.fx.arc-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape ArcTo]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe ArcTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:radius-x [:setter lifecycle/scalar :coerce double :default 0]
            :radius-y [:setter lifecycle/scalar :coerce double :default 0]
            :x-axis-rotation [:setter lifecycle/scalar :coerce double :default 0]
            :large-arc-flag [:setter lifecycle/scalar :default false]
            :sweep-flag [:setter lifecycle/scalar :default false]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))