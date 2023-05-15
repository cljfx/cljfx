(ns cljfx.jdk.fx.spot-light
  "Part of public API"
  (:require [cljfx.fx.point-light :as fx.point-light]
            [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene SpotLight]))

(def props
  (merge
    fx.point-light/props
    (composite/props SpotLight
      :direction [:setter lifecycle/scalar :coerce coerce/point-3d :default {:x 0.0 :y 0.0 :z 1.0}]
      :falloff [:setter lifecycle/scalar :coerce double :default 1.0]
      :inner-angle [:setter lifecycle/scalar :coerce double :default 0.0]
      :outer-angle [:setter lifecycle/scalar :coerce double :default 30.0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe SpotLight
      :ctor []
      :props props)
    :spot-light))