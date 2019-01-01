(ns cljfx.fx.transform
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.transform Translate Rotate Affine Scale Shear Transform]))

(set! *warn-on-reflection* true)

(def transform
  (lifecycle.composite/describe Transform
    :props {:on-transform-changed [:setter lifecycle/event-handler :coerce coerce/event-handler]}))

(def affine
  (lifecycle.composite/describe Affine
    :ctor []
    :extends [transform]
    :props {:mxx [:setter lifecycle/scalar :coerce double :default 1.0]
            :mxy [:setter lifecycle/scalar :coerce double :default 0.0
                  :mxz [:setter lifecycle/scalar :coerce double :default 0.0]
                  :myx [:setter lifecycle/scalar :coerce double :default 0.0]
                  :myy [:setter lifecycle/scalar :coerce double :default 1.0]
                  :myz [:setter lifecycle/scalar :coerce double :default 0.0]
                  :mzx [:setter lifecycle/scalar :coerce double :default 0.0]
                  :mzy [:setter lifecycle/scalar :coerce double :default 0.0]
                  :mzz [:setter lifecycle/scalar :coerce double :default 1.0]
                  :tx [:setter lifecycle/scalar :coerce double :default 0.0]
                  :ty [:setter lifecycle/scalar :coerce double :default 0.0]
                  :tz [:setter lifecycle/scalar :coerce double :default 0.0]]}))

(def rotate
  (lifecycle.composite/describe Rotate
    :ctor []
    :extends [transform]
    :props {:angle [:setter lifecycle/scalar :coerce double :default 0.0]
            :axis [:setter lifecycle/scalar :coerce coerce/point-3d :default :z-axis]
            :pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-z [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def scale
  (lifecycle.composite/describe Scale
    :ctor []
    :extends [transform]
    :props {:pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-z [:setter lifecycle/scalar :coerce double :default 0.0]
            :x [:setter lifecycle/scalar :coerce double :default 1.0]
            :y [:setter lifecycle/scalar :coerce double :default 1.0]
            :z [:setter lifecycle/scalar :coerce double :default 1.0]}))

(def shear
  (lifecycle.composite/describe Shear
    :ctor []
    :extends [transform]
    :props {:pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def translate
  (lifecycle.composite/describe Translate
    :ctor []
    :extends [transform]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]
            :z [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def keyword->lifecycle
  {:affine affine
   :rotate rotate
   :scale scale
   :shear shear
   :translate translate})