(ns cljfx.fx.triangle-mesh
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.shape TriangleMesh]))

(def lifecycle
  (lifecycle.composite/describe TriangleMesh
    :ctor []
    :props {:vertex-format [:setter lifecycle/scalar :coerce coerce/vertex-format]
            :faces [:list lifecycle/scalar :coerce #(map int %)]
            :face-smoothing-groups [:list lifecycle/scalar :coerce #(map int %)]
            :normals [:list lifecycle/scalar :coerce #(map float %)]
            :points [:list lifecycle/scalar :coerce #(map float %)]
            :tex-coords [:list lifecycle/scalar :coerce #(map float %)]}))