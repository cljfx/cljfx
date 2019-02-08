(ns cljfx.fx.point-light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.light-base :as fx.light-base])
  (:import [javafx.scene PointLight]))

(set! *warn-on-reflection* true)

(def props
  fx.light-base/props)

(def lifecycle
  (lifecycle.composite/describe PointLight
    :ctor []
    :props props))
