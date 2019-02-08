(ns cljfx.fx.transform
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.transform Transform]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props Transform
    :on-transform-changed [:setter lifecycle/event-handler :coerce coerce/event-handler]))
