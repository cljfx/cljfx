(ns cljfx.fx.camera
  "Part of a public API"
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.composite :as composite]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene Camera]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (composite/props Camera
      :near-clip [:setter lifecycle/scalar :coerce double :default 0.1]
      :far-clip [:setter lifecycle/scalar :coerce double :default 100])))
