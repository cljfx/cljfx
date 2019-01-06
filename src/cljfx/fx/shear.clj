(ns cljfx.fx.shear
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Shear]))

(def lifecycle
  (lifecycle.composite/describe Shear
    :ctor []
    :extends [fx.transform/lifecycle]
    :props {:pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]}))