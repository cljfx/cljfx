(ns cljfx.fx.camera
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.lifecycle.composite :as lifecycle.composite])
  (:import [javafx.scene Camera]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Camera
    :props {:near-clip [:setter lifecycle/scalar :coerce double :default 0.1]
            :far-clip [:setter lifecycle/scalar :coerce double :default 100]}))
