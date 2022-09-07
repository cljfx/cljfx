(ns cljfx.fx.v-line-to
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape VLineTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (composite/props VLineTo
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe VLineTo
      :ctor []
      :props props)
    :v-line-to))
