(ns cljfx.fx.phong-material
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.paint PhongMaterial]))

(set! *warn-on-reflection* true)

(def props
  (composite/props PhongMaterial
    :bump-map [:setter lifecycle/scalar :coerce coerce/image]
    :diffuse-color [:setter lifecycle/scalar :coerce coerce/color :default :white]
    :diffuse-map [:setter lifecycle/scalar :coerce coerce/image]
    :self-illumination-map [:setter lifecycle/scalar :coerce coerce/image]
    :specular-color [:setter lifecycle/scalar :coerce coerce/color]
    :specular-map [:setter lifecycle/scalar :coerce coerce/image]
    :specular-power [:setter lifecycle/scalar :coerce double :default 32.0]))

(def lifecycle
  (composite/describe PhongMaterial
    :ctor []
    :props props))
