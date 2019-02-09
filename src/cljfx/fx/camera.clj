(ns cljfx.fx.camera
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.composite :as composite])
  (:import [javafx.scene Camera]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Camera
    :near-clip [:setter lifecycle/scalar :coerce double :default 0.1]
    :far-clip [:setter lifecycle/scalar :coerce double :default 100]))
