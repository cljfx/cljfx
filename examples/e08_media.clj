(ns e08-media
  (:require [cljfx.api :as fx]))

(def *state
  (atom {:media-state :playing
         :volume 1}))

(defn button [{:keys [text event-type]}]
  {:fx/type :button
   :text text
   :pref-width 100
   :on-action {:event/type event-type}})

(defn volume-slider [{:keys [volume]}]
  {:fx/type :slider
   :min 0
   :max 1
   :value volume
   :on-value-changed {:event/type ::set-volume}})

(def media-url
  "https://www.sample-videos.com/video123/mp4/480/big_buck_bunny_480p_1mb.mp4")

(defn media-view [{:keys [media-state volume]}]
  {:fx/type :media-view
   :fit-width 640
   :fit-height 480
   :media-player {:fx/type :media-player
                  :state media-state
                  :volume volume
                  :on-end-of-media {:event/type ::stop}
                  :media {:fx/type :media
                          :source media-url}}})

(def stopped-label
  {:fx/type :label
   :pref-width 640
   :pref-height 480
   :alignment :center
   :text "Stopped!"})

(defn root [{:keys [media-state volume]}]
  {:fx/type :stage
   :showing true
   :on-close-request {:event/type ::stop}
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [(if (not= :stopped media-state)
                               {:fx/type media-view
                                :media-state media-state
                                :volume volume}
                               stopped-label)
                             {:fx/type :h-box
                              :padding 10
                              :spacing 10
                              :alignment :center-left
                              :children [{:fx/type button
                                          :text "Stop"
                                          :event-type ::stop}
                                         (if (= :playing media-state)
                                           {:fx/type button
                                            :text "Pause"
                                            :event-type ::pause}
                                           {:fx/type button
                                            :text "Play"
                                            :event-type ::play})
                                         {:fx/type volume-slider
                                          :volume volume}]}]}}})

(defn map-event-handler [e]
  (case (:event/type e)
    ::stop (swap! *state assoc :media-state :stopped)
    ::play (swap! *state assoc :media-state :playing)
    ::pause (swap! *state assoc :media-state :paused)
    ::set-volume (swap! *state assoc :volume (:fx/event e))
    (prn e)))

(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler map-event-handler}
    :middleware (fx/wrap-map-desc root)))

(fx/mount-renderer *state renderer)
