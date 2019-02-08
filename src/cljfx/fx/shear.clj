(ns cljfx.fx.shear
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Shear]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transform/props
    (lifecycle.composite/props Shear
      :pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
      :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
      :x [:setter lifecycle/scalar :coerce double :default 0.0]
      :y [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle.composite/describe Shear
    :ctor []
    :props props))
