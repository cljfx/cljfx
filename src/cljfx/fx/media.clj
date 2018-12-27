(ns cljfx.fx.media
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.media Media MediaPlayer MediaView]
           [javafx.scene.image ImageView]))

(set! *warn-on-reflection* true)

(def media
  (lifecycle.composite/describe Media
    :ctor [:source]
    :default-prop [:source prop/extract-single]
    :props {:source [mutator/forbidden lifecycle/scalar]
            :on-error [:setter lifecycle/event-handler :coerce coerce/runnable]}))

(def media-player
  (lifecycle.composite/describe MediaPlayer
    :ctor [:media]
    :default-prop [:media prop/extract-single]
    :on-delete #(.dispose ^MediaPlayer %)
    :props {:media [mutator/forbidden lifecycle/dynamic-hiccup]
            :state [(mutator/setter #(case %2
                                       :played (.play ^MediaPlayer %1)
                                       :paused (.pause ^MediaPlayer %1)
                                       :stopped (.stop ^MediaPlayer %1)))
                    lifecycle/scalar
                    :default :stopped]
            :audio-spectrum-interval [:setter lifecycle/scalar :coerce double :default 0.1]
            :audio-spectrum-listener [:setter lifecycle/event-handler
                                      :coerce coerce/audio-spectrum-listener]
            :audio-spectrum-num-bands [:setter lifecycle/scalar :coerce int :default 128]
            :audio-spectrum-threshold [:setter lifecycle/scalar :coerce int :default -60]
            :auto-play [:setter lifecycle/scalar :default false]
            :balance [:setter lifecycle/scalar :coerce double :default 0]
            :cycle-count [:setter lifecycle/scalar :coerce int :default 1]
            :mute [:setter lifecycle/scalar :default false]
            :on-end-of-media [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-error [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-halted [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-marker [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-paused [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-playing [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-ready [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-repeat [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-stalled [:setter lifecycle/event-handler :coerce coerce/runnable]
            :on-stopped [:setter lifecycle/event-handler :coerce coerce/runnable]
            :rate [:setter lifecycle/scalar :coerce double :default 1]
            :start-time [:setter lifecycle/scalar
                         :coerce coerce/duration :default :zero]
            :stop-time [:setter lifecycle/dynamic-hiccup
                        :coerce coerce/duration
                        :default :indefinite]
            :volume [:setter lifecycle/scalar :coerce double :default 1]}))

(def media-view
  (lifecycle.composite/describe MediaView
    :ctor []
    :extends [fx.scene/node]
    :default-prop [:media-player prop/extract-single]
    :props {:media-player [:setter lifecycle/dynamic-hiccup]
            :on-error [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :preserve-ratio [:setter lifecycle/scalar :default true]
            :smooth [:setter lifecycle/scalar :default ImageView/SMOOTH_DEFAULT]
            :x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]
            :fit-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :fit-height [:setter lifecycle/scalar :coerce double :default 0.0]
            :viewport [:setter lifecycle/scalar :coerce coerce/rectangle-2d]}))

(def tag->lifecycle
  {:media media
   :media-player media-player
   :media-view media-view})
