(ns cljfx.fx.translate
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Translate]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transform/props
    (lifecycle.composite/props Translate
      :x [:setter lifecycle/scalar :coerce double :default 0.0]
      :y [:setter lifecycle/scalar :coerce double :default 0.0]
      :z [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle.composite/describe Translate
    :ctor []
    :props props))
