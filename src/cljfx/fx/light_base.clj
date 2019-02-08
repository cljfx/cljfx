(ns cljfx.fx.light-base
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.node :as fx.node]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene LightBase]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (lifecycle.composite/props LightBase
      :color [:setter lifecycle/scalar :coerce coerce/color :default :white]
      :light-on [:setter lifecycle/scalar :default true])))
