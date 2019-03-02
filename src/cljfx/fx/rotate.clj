(ns cljfx.fx.rotate
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Rotate]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transform/props
    (composite/props Rotate
      :angle [:setter lifecycle/scalar :coerce double :default 0.0]
      :axis [:setter lifecycle/scalar :coerce coerce/point-3d :default :z-axis]
      :pivot-x [:setter lifecycle/scalar :coerce double :default 0.0]
      :pivot-y [:setter lifecycle/scalar :coerce double :default 0.0]
      :pivot-z [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (composite/describe Rotate
    :ctor []
    :props props))
