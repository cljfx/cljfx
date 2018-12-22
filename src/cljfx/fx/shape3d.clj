(ns cljfx.fx.shape3d
  (:require [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.lifecycle.composite :as lifecycle.composite])
  (:import [javafx.scene.shape DrawMode CullFace Shape3D TriangleMesh Sphere MeshView
                               Cylinder Box]
           [javafx.scene LightBase PointLight AmbientLight]
           [javafx.scene.paint PhongMaterial]))

(set! *warn-on-reflection* true)

(def light-base
  (lifecycle.composite/describe LightBase
    :extends [fx.scene/node]
    :props {:color [:setter prop/scalar :coerce coerce/color]
            :light-on [:setter prop/scalar :default true]}))

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
    :props {:cull-face [:setter prop/scalar :coerce (coerce/enum CullFace) :default :back]
            :draw-mode [:setter prop/scalar :coerce (coerce/enum DrawMode) :default :fill]
            :material [:setter prop/component]}))

(def box
  (lifecycle.composite/describe Box
    :ctor []
    :extends [shape3d]
    :props {:depth [:setter prop/scalar :coerce double :default 2.0]
            :height [:setter prop/scalar :coerce double :default 2.0]
            :width [:setter prop/scalar :coerce double :default 2.0]}))

(def cylinder
  (lifecycle.composite/describe Cylinder
    :ctor []
    :extends [shape3d]
    :props {:height [:setter prop/scalar :coerce double :default 2.0]
            :radius [:setter prop/scalar :coerce double :default 1.0]}))

(def mesh-view
  (lifecycle.composite/describe MeshView
    :ctor []
    :extends [shape3d]
    :default-prop [:mesh prop/extract-single]
    :props {:mesh [:setter prop/component]}))

(def triangle-mesh
  (lifecycle.composite/describe TriangleMesh
    :ctor []
    :props {:vertex-format [:setter prop/scalar :coerce coerce/vertex-format]
            :faces [:list prop/scalar :coerce #(map int %)]
            :face-smoothing-groups [:list prop/scalar :coerce #(map int %)]
            :normals [:list prop/scalar :coerce #(map float %)]
            :points [:list prop/scalar :coerce #(map float %)]
            :tex-coords [:list prop/scalar :coerce #(map float %)]}))

(def sphere
  (lifecycle.composite/describe Sphere
    :ctor []
    :extends [shape3d]
    :default-prop [:radius prop/extract-single]
    :props {:radius [:setter prop/scalar :coerce double :default 1.0]}))

(def phong-material
  (lifecycle.composite/describe PhongMaterial
    :ctor []
    :props {:bump-map [:setter prop/scalar :coerce coerce/image]
            :diffuse-color [:setter prop/scalar :coerce coerce/color :default :white]
            :diffuse-map [:setter prop/scalar :coerce coerce/image]
            :self-illumination-map [:setter prop/scalar :coerce coerce/image]
            :specular-color [:setter prop/scalar :coerce coerce/color]
            :specular-map [:setter prop/scalar :coerce coerce/image]
            :specular-power [:setter prop/scalar :coerce double :default 32.0]}))

(def tag->lifecycle
  {:box box
   :cylinder cylinder
   :mesh-view mesh-view
   :triangle-mesh triangle-mesh
   :sphere sphere
   :ambient-light ambient-light
   :point-light point-light
   :phong-material phong-material})