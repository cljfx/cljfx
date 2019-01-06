(ns cljfx.fx.shape3d
  (:require [cljfx.coerce :as coerce]
            [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene.shape DrawMode CullFace Shape3D]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Shape3D
    :extends [fx.node/lifecycle]
    :props {:cull-face [:setter lifecycle/scalar :coerce (coerce/enum CullFace) :default :back]
            :draw-mode [:setter lifecycle/scalar :coerce (coerce/enum DrawMode) :default :fill]
            :material [:setter lifecycle/dynamic]}))
