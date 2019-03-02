(ns cljfx.fx.triangle-mesh
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.shape TriangleMesh VertexFormat]))

(set! *warn-on-reflection* true)

(defn vertex-format [x]
  (cond
    (instance? VertexFormat x) x
    (= :point-texcoord x) VertexFormat/POINT_TEXCOORD
    (= :point-normal-texcoord) VertexFormat/POINT_NORMAL_TEXCOORD
    :else (coerce/fail VertexFormat x)))

(def props
  (composite/props TriangleMesh
    :vertex-format [:setter lifecycle/scalar :coerce vertex-format]
    :faces [:list lifecycle/scalar :coerce #(map int %)]
    :face-smoothing-groups [:list lifecycle/scalar :coerce #(map int %)]
    :normals [:list lifecycle/scalar :coerce #(map float %)]
    :points [:list lifecycle/scalar :coerce #(map float %)]
    :tex-coords [:list lifecycle/scalar :coerce #(map float %)]))

(def lifecycle
  (composite/describe TriangleMesh
    :ctor []
    :props props))
