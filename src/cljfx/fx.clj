(ns cljfx.fx
  (:require [cljfx.fx.effect :as fx.effect]
            [cljfx.fx.shape :as fx.shape]
            [cljfx.fx.camera :as fx.camera]
            [cljfx.fx.shape3d :as fx.shape3d]
            [cljfx.fx.media :as fx.media]
            [cljfx.fx.chart :as fx.chart]
            [cljfx.fx.transform :as fx.transform]
            [cljfx.fx.control :as fx.control]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.fx.web :as fx.web]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.fx.stage :as fx.stage]))

(def tag->lifecycle
  (merge
    fx.effect/tag->lifecycle
    fx.shape/tag->lifecycle
    fx.scene/tag->lifecycle
    fx.media/tag->lifecycle
    fx.shape3d/tag->lifecycle
    fx.transform/tag->lifecycle
    fx.chart/tag->lifecycle
    fx.camera/tag->lifecycle
    fx.control/tag->lifecycle
    fx.pane/tag->lifecycle
    fx.web/tag->lifecycle
    fx.stage/tag->lifecycle))
