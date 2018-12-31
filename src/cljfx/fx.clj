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

(def keyword->lifecycle
  (merge
    fx.effect/keyword->lifecycle
    fx.shape/keyword->lifecycle
    fx.scene/keyword->lifecycle
    fx.media/keyword->lifecycle
    fx.shape3d/keyword->lifecycle
    fx.transform/keyword->lifecycle
    fx.chart/keyword->lifecycle
    fx.camera/keyword->lifecycle
    fx.control/keyword->lifecycle
    fx.pane/keyword->lifecycle
    fx.web/keyword->lifecycle
    fx.stage/keyword->lifecycle))
