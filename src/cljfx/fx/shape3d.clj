(ns cljfx.fx.shape3d
  (:require [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.shape DrawMode CullFace Shape3D TriangleMesh Sphere MeshView
                               Cylinder Box]
           [javafx.scene LightBase PointLight AmbientLight]
           [javafx.scene.paint PhongMaterial]))

(set! *warn-on-reflection* true)

(def light-base
  (lifecycle.composite/describe LightBase
    :extends [fx.scene/node]
    :props {:color [:setter lifecycle/scalar :coerce coerce/color]
            :light-on [:setter lifecycle/scalar :default true]}))

(def ambient-light
  (lifecycle.composite/describe AmbientLight
    :ctor []
    :extends [light-base]))

(def point-light
  (lifecycle.composite/describe PointLight
    :ctor []
    :extends [light-base]))

(def shape3d
  (lifecycle.composite/describe Shape3D
    :extends [fx.scene/node]
    :props {:cull-face [:setter lifecycle/scalar :coerce (coerce/enum CullFace) :default :back]
            :draw-mode [:setter lifecycle/scalar :coerce (coerce/enum DrawMode) :default :fill]
            :material [:setter lifecycle/dynamic]}))

(def box
  (lifecycle.composite/describe Box
    :ctor []
    :extends [shape3d]
    :props {:depth [:setter lifecycle/scalar :coerce double :default 2.0]
            :height [:setter lifecycle/scalar :coerce double :default 2.0]
            :width [:setter lifecycle/scalar :coerce double :default 2.0]}))

(def cylinder
  (lifecycle.composite/describe Cylinder
    :ctor []
    :extends [shape3d]
    :props {:height [:setter lifecycle/scalar :coerce double :default 2.0]
            :radius [:setter lifecycle/scalar :coerce double :default 1.0]}))

(def mesh-view
  (lifecycle.composite/describe MeshView
    :ctor []
    :extends [shape3d]
    :props {:mesh [:setter lifecycle/dynamic]}))

(def triangle-mesh
  (lifecycle.composite/describe TriangleMesh
    :ctor []
    :props {:vertex-format [:setter lifecycle/scalar :coerce coerce/vertex-format]
            :faces [:list lifecycle/scalar :coerce #(map int %)]
            :face-smoothing-groups [:list lifecycle/scalar :coerce #(map int %)]
            :normals [:list lifecycle/scalar :coerce #(map float %)]
            :points [:list lifecycle/scalar :coerce #(map float %)]
            :tex-coords [:list lifecycle/scalar :coerce #(map float %)]}))

(def sphere
  (lifecycle.composite/describe Sphere
    :ctor []
    :extends [shape3d]
    :props {:radius [:setter lifecycle/scalar :coerce double :default 1.0]}))

(def phong-material
  (lifecycle.composite/describe PhongMaterial
    :ctor []
    :props {:bump-map [:setter lifecycle/scalar :coerce coerce/image]
            :diffuse-color [:setter lifecycle/scalar :coerce coerce/color :default :white]
            :diffuse-map [:setter lifecycle/scalar :coerce coerce/image]
            :self-illumination-map [:setter lifecycle/scalar :coerce coerce/image]
            :specular-color [:setter lifecycle/scalar :coerce coerce/color]
            :specular-map [:setter lifecycle/scalar :coerce coerce/image]
            :specular-power [:setter lifecycle/scalar :coerce double :default 32.0]}))

(def keyword->lifecycle
  {:box box
   :cylinder cylinder
   :mesh-view mesh-view
   :triangle-mesh triangle-mesh
   :sphere sphere
   :ambient-light ambient-light
   :point-light point-light
   :phong-material phong-material})