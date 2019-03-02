(ns cljfx.fx.ambient-light
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.light-base :as fx.light-base])
  (:import [javafx.scene AmbientLight]))

(set! *warn-on-reflection* true)

(def props
  fx.light-base/props)

(def lifecycle
  (composite/describe AmbientLight
    :ctor []
    :props props))
