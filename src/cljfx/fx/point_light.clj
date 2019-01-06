(ns cljfx.fx.point-light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.light-base :as fx.light-base])
  (:import [javafx.scene PointLight]))

(def lifecycle
  (lifecycle.composite/describe PointLight
    :ctor []
    :extends [fx.light-base/lifecycle]))