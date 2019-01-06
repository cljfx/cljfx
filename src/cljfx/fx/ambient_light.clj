(ns cljfx.fx.ambient-light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.light-base :as fx.light-base])
  (:import [javafx.scene AmbientLight]))

(def lifecycle
  (lifecycle.composite/describe AmbientLight
    :ctor []
    :extends [fx.light-base/lifecycle]))