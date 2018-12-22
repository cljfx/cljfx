(ns cljfx.fx.media
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene])
  (:import [javafx.scene.media Media MediaPlayer MediaView]
           [javafx.scene.image ImageView]))

(set! *warn-on-reflection* true)

(def media
  (lifecycle.composite/describe Media
    :ctor [:source]
    :default-prop [:source prop/extract-single]
    :props {:source [(prop/ctor-only) prop/scalar]
            :on-error [:setter prop/scalar :coerce coerce/runnable]}))

(def media-player
  (lifecycle.composite/describe MediaPlayer
    :ctor [:media]
    :default-prop [:media prop/extract-single]
    :on-delete #(.dispose ^MediaPlayer %)
    :props {:media [(prop/ctor-only) prop/component]
            :state [(prop/setter #(case %2
                                    :played (.play ^MediaPlayer %1)
                                    :paused (.pause ^MediaPlayer %1)
                                    :stopped (.stop ^MediaPlayer %1)))
                    prop/scalar
                    :default :stopped]
            :audio-spectrum-interval [:setter prop/scalar :coerce double :default 0.1]
            :audio-spectrum-listener [:setter prop/scalar
                                      :coerce coerce/audio-spectrum-listener]
            :audio-spectrum-num-bands [:setter prop/scalar :coerce int :default 128]
            :audio-spectrum-threshold [:setter prop/scalar :coerce int :default -60]
            :auto-play [:setter prop/scalar :default false]
            :balance [:setter prop/scalar :coerce double :default 0]
            :cycle-count [:setter prop/scalar :coerce int :default 1]
            :mute [:setter prop/scalar :default false]
            :on-end-of-media [:setter prop/scalar :coerce coerce/runnable]
            :on-error [:setter prop/scalar :coerce coerce/runnable]
            :on-halted [:setter prop/scalar :coerce coerce/runnable]
            :on-marker [:setter prop/scalar :coerce coerce/event-handler]
            :on-paused [:setter prop/scalar :coerce coerce/runnable]
            :on-playing [:setter prop/scalar :coerce coerce/runnable]
            :on-ready [:setter prop/scalar :coerce coerce/runnable]
            :on-repeat [:setter prop/scalar :coerce coerce/runnable]
            :on-stalled [:setter prop/scalar :coerce coerce/runnable]
            :on-stopped [:setter prop/scalar :coerce coerce/runnable]
            :rate [:setter prop/scalar :coerce double :default 1]
            :start-time [:setter prop/scalar
                         :coerce coerce/duration :default :zero]
            :stop-time [:setter prop/component
                        :coerce coerce/duration
                        :default :indefinite]
            :volume [:setter prop/scalar :coerce double :default 1]}))

(def media-view
  (lifecycle.composite/describe MediaView
    :ctor []
    :extends [fx.scene/node]
    :default-prop [:media-player prop/extract-single]
    :props {:media-player [:setter prop/component]
            :on-error [:setter prop/scalar :coerce coerce/event-handler]
            :preserve-ratio [:setter prop/scalar :default true]
            :smooth [:setter prop/scalar :default ImageView/SMOOTH_DEFAULT]
            :x [:setter prop/scalar :coerce double :default 0.0]
            :y [:setter prop/scalar :coerce double :default 0.0]
            :fit-width [:setter prop/scalar :coerce double :default 0.0]
            :fit-height [:setter prop/scalar :coerce double :default 0.0]
            :viewport [:setter prop/scalar :coerce coerce/rectangle-2d]}))

(def tag->lifecycle
  {:media media
   :media-player media-player
   :media-view media-view})
