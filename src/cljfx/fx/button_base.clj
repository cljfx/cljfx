(ns cljfx.fx.button-base
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control ButtonBase]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.labeled/props
    (lifecycle.composite/props ButtonBase
      :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler])))
