(ns cljfx.fx.affine
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Affine]))

(def lifecycle
  (lifecycle.composite/describe Affine
    :ctor []
    :extends [fx.transform/lifecycle]
    :props {:mxx [:setter lifecycle/scalar :coerce double :default 1.0]
            :mxy [:setter lifecycle/scalar :coerce double :default 0.0]
            :mxz [:setter lifecycle/scalar :coerce double :default 0.0]
            :myx [:setter lifecycle/scalar :coerce double :default 0.0]
            :myy [:setter lifecycle/scalar :coerce double :default 1.0]
            :myz [:setter lifecycle/scalar :coerce double :default 0.0]
            :mzx [:setter lifecycle/scalar :coerce double :default 0.0]
            :mzy [:setter lifecycle/scalar :coerce double :default 0.0]
            :mzz [:setter lifecycle/scalar :coerce double :default 1.0]
            :tx [:setter lifecycle/scalar :coerce double :default 0.0]
            :ty [:setter lifecycle/scalar :coerce double :default 0.0]
            :tz [:setter lifecycle/scalar :coerce double :default 0.0]}))