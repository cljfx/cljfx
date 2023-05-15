(ns cljfx.jdk.fx.directional-light
  "Part of public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.light-base :as fx.light-base])
  (:import [javafx.scene DirectionalLight]))

(def props
  (merge
    fx.light-base/props
    (composite/props DirectionalLight
      :direction [:setter lifecycle/scalar :coerce coerce/point-3d :default {:x 0.0 :y 0.0 :z 1.0}])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe DirectionalLight
      :ctor []
      :props props)
    :directional-light))
