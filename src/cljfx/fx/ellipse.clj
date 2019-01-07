(ns cljfx.fx.ellipse
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Ellipse]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Ellipse
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:center-x [:setter lifecycle/scalar :coerce double :default 0]
            :center-y [:setter lifecycle/scalar :coerce double :default 0]
            :radius-x [:setter lifecycle/scalar :coerce double :default 0]
            :radius-y [:setter lifecycle/scalar :coerce double :default 0]}))