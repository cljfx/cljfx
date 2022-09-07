(ns cljfx.fx.translate
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transform :as fx.transform])
  (:import [javafx.scene.transform Translate]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transform/props
    (composite/props Translate
      :x [:setter lifecycle/scalar :coerce double :default 0.0]
      :y [:setter lifecycle/scalar :coerce double :default 0.0]
      :z [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Translate
      :ctor []
      :props props)
    :translate))
