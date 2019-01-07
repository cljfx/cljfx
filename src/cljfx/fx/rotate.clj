(ns cljfx.fx.rotate
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Rotate]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Rotate
    :ctor []
    :extends [fx.transform/lifecycle]
    :props {:angle [:setter lifecycle/scalar :coerce double :default 0.0]
            :axis [:setter lifecycle/scalar :coerce coerce/point-3d :default :z-axis]
            :pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :pivot-z [:setter lifecycle/scalar :coerce double :default 0.0]}))