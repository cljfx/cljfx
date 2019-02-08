(ns cljfx.fx.sub-scene
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene SubScene]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (lifecycle.composite/props SubScene
      :root [:setter lifecycle/dynamic]
      :width [:setter lifecycle/scalar :coerce double :default 0]
      :height [:setter lifecycle/scalar :coerce double :default 0]
      :camera [:setter lifecycle/dynamic]
      :fill [:setter lifecycle/scalar :coerce coerce/paint]
      :user-agent-stylesheet [:setter lifecycle/scalar])))

(def lifecycle
  (lifecycle.composite/describe SubScene
    :ctor [:root :width :height]
    :props props))
