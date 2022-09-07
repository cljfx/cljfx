(ns cljfx.fx.lighting
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Lighting]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Lighting
    :light [:setter lifecycle/dynamic]
    :bump-input [:setter lifecycle/dynamic]
    :content-input [:setter lifecycle/dynamic]
    :diffuse-constant [:setter lifecycle/scalar :coerce double :default 1]
    :specular-constant [:setter lifecycle/scalar :coerce double :default 0.3]
    :specular-exponent [:setter lifecycle/scalar :coerce double :default 0.3]
    :surface-scale [:setter lifecycle/scalar :coerce double :default 1.5]))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Lighting
      :ctor []
      :props props)
    :lighting))
