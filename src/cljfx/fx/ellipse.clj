(ns cljfx.fx.ellipse
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Ellipse]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (lifecycle.composite/props Ellipse
      :center-x [:setter lifecycle/scalar :coerce double :default 0]
      :center-y [:setter lifecycle/scalar :coerce double :default 0]
      :radius-x [:setter lifecycle/scalar :coerce double :default 0]
      :radius-y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle.composite/describe Ellipse
    :ctor []
    :props props))
