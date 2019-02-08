(ns cljfx.fx.media
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.media Media]))

(set! *warn-on-reflection* true)

(def props
  (lifecycle.composite/props Media
    :source [mutator/forbidden lifecycle/scalar]
    :on-error [:setter lifecycle/event-handler :coerce coerce/runnable]))

(def lifecycle
  (lifecycle.composite/describe Media
    :ctor [:source]
    :props props))
