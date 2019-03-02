(ns cljfx.fx.shape3d
  "Part of a public API"
  (:require [cljfx.coerce :as coerce]
            [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene.shape DrawMode CullFace Shape3D]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (composite/props Shape3D
      :cull-face [:setter lifecycle/scalar :coerce (coerce/enum CullFace) :default :back]
      :draw-mode [:setter lifecycle/scalar :coerce (coerce/enum DrawMode) :default :fill]
      :material [:setter lifecycle/dynamic])))
