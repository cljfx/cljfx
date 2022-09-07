(ns cljfx.fx.ellipse
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Ellipse]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Ellipse
      :center-x [:setter lifecycle/scalar :coerce double :default 0]
      :center-y [:setter lifecycle/scalar :coerce double :default 0]
      :radius-x [:setter lifecycle/scalar :coerce double :default 0]
      :radius-y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Ellipse
      :ctor []
      :props props)
    :ellipse))
