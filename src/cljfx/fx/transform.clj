(ns cljfx.fx.transform
  (:require [cljfx.prop :as prop]
            [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.transform Translate Rotate Affine Scale Shear Transform]))

(set! *warn-on-reflection* true)

(def transform
  (lifecycle.composite/describe Transform
    :props {:on-transform-changed [:setter prop/scalar :coerce coerce/event-handler]}))

(def affine
  (lifecycle.composite/describe Affine
    :ctor []
    :extends [transform]
    :props {:mxx [:setter prop/scalar :coerce coerce/as-double :default 1.0]
            :mxy [:setter prop/scalar :coerce coerce/as-double :default 0.0
                  :mxz [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :myx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :myy [:setter prop/scalar :coerce coerce/as-double :default 1.0]
                  :myz [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :mzx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :mzy [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :mzz [:setter prop/scalar :coerce coerce/as-double :default 1.0]
                  :tx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :ty [:setter prop/scalar :coerce coerce/as-double :default 0.0]
                  :tz [:setter prop/scalar :coerce coerce/as-double :default 0.0]]}))

(def rotate
  (lifecycle.composite/describe Rotate
    :ctor []
    :extends [transform]
    :props {:angle [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :axis [:setter prop/scalar :coerce coerce/point-3d :default :z-axis]
            :pivot-x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :pivot-y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :pivot-z [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def scale
  (lifecycle.composite/describe Scale
    :ctor []
    :extends [transform]
    :props {:pivot-x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :pivot-y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :pivot-z [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :x [:setter prop/scalar :coerce coerce/as-double :default 1.0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 1.0]
            :z [:setter prop/scalar :coerce coerce/as-double :default 1.0]}))

(def shear
  (lifecycle.composite/describe Shear
    :ctor []
    :extends [transform]
    :props {:pivot-x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :pivot-y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def translate
  (lifecycle.composite/describe Translate
    :ctor []
    :extends [transform]
    :props {:x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :z [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def tag->lifecycle
  {:transform/affine affine
   :transform/rotate rotate
   :transform/scale scale
   :transform/shear shear
   :transform/translate translate})