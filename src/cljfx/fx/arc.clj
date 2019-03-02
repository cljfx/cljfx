(ns cljfx.fx.arc
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.shape :as fx.shape]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.shape Arc ArcType]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Arc
      :center-x [:setter lifecycle/scalar :coerce double :default 0]
      :center-y [:setter lifecycle/scalar :coerce double :default 0]
      :length [:setter lifecycle/scalar :coerce double :default 0]
      :radius-x [:setter lifecycle/scalar :coerce double :default 0]
      :radius-y [:setter lifecycle/scalar :coerce double :default 0]
      :start-angle [:setter lifecycle/scalar :coerce double :default 0]
      :type [:setter lifecycle/scalar :coerce (coerce/enum ArcType) :default :open])))

(def lifecycle
  (composite/describe Arc
    :ctor []
    :props props))
