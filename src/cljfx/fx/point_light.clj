(ns cljfx.fx.point-light
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.light-base :as fx.light-base]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene PointLight]))

(set! *warn-on-reflection* true)

(def props
  fx.light-base/props)

(def lifecycle
  (lifecycle/annotate
    (composite/describe PointLight
      :ctor []
      :props props)
    :point-light))
