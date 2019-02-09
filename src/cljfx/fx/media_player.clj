(ns cljfx.fx.media-player
  (:require [cljfx.composite :as composite]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.media MediaPlayer AudioSpectrumListener]))

(set! *warn-on-reflection* true)

(defn- audio-spectrum-listener [x]
  (cond
    (instance? AudioSpectrumListener x)
    x

    (fn? x)
    (reify AudioSpectrumListener
      (spectrumDataUpdate [_ timestamp duration magnitudes phases]
        (x {:timestamp timestamp
            :duration duration
            :magnitudes (into [] magnitudes)
            :phases (into [] phases)})))

    :else
    (coerce/fail AudioSpectrumListener x)))

(def lifecycle
  (-> MediaPlayer
      (composite/describe
        :ctor [:media]
        :props {:media [mutator/forbidden lifecycle/dynamic]
                :state [(mutator/setter #(case %2
                                           :playing (.play ^MediaPlayer %1)
                                           :paused (.pause ^MediaPlayer %1)
                                           :stopped (.stop ^MediaPlayer %1)))
                        lifecycle/scalar
                        :default :stopped]
                :audio-spectrum-interval [:setter lifecycle/scalar :coerce double :default 0.1]
                :audio-spectrum-listener [:setter lifecycle/event-handler
                                          :coerce audio-spectrum-listener]
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
                :start-time [:setter lifecycle/scalar :coerce coerce/duration
                             :default :zero]
                :stop-time [:setter lifecycle/dynamic :coerce coerce/duration
                            :default :indefinite]
                :volume [:setter lifecycle/scalar :coerce double :default 1]})
      (lifecycle/wrap-on-delete #(.dispose ^MediaPlayer %))))
