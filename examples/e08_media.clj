(ns e08-media
  (:require [cljfx.api :as cljfx]))

(def *state
  (atom {:media-state :playing
         :volume 1}))

(defn button [text event-type]
  [:button
   {:text text
    :pref-width 100
    :on-action {:event/type event-type}}])

(defn volume-slider [volume]
  [:slider
   {:min 0
    :max 1
    :value volume
    :on-value-changed {:event/type ::set-volume}}])

(def media-url
  "https://www.sample-videos.com/video123/mp4/480/big_buck_bunny_480p_1mb.mp4")

(defn media-view [media-state volume]
  [:media-view
   {:fit-width 640
    :fit-height 480
    :media-player
    [:media-player
     {:state media-state
      :volume volume
      :on-end-of-media {:event/type ::stop}
      :media
      [:media
       {:source media-url}]}]}])

(def stopped-label
  [:label
   {:pref-width 640
    :pref-height 480
    :alignment :center
    :text "Stopped!"}])

(defn root [{:keys [media-state volume]}]
  [:stage
   {:showing true
    :on-close-request {:event/type ::stop}
    :scene [:scene
            {:root [:v-box
                    {:children [(if (not= :stopped media-state)
                                  [media-view media-state volume]
                                  stopped-label)
                                [:h-box
                                 {:padding 10
                                  :spacing 10
                                  :alignment :center-left
                                  :children [[button "Stop" ::stop]
                                             (if (= :playing media-state)
                                               [button "Pause" ::pause]
                                               [button "Play" ::play])
                                             [volume-slider volume]]}]]}]}]}])

(defn map-event-handler [e]
  (case (:event/type e)
    ::stop (swap! *state assoc :media-state :stopped)
    ::play (swap! *state assoc :media-state :playing)
    ::pause (swap! *state assoc :media-state :paused)
    ::set-volume (swap! *state assoc :volume (:cljfx/event e))
    (prn e)))

(def app
  (cljfx/create-app
    :opts {:cljfx.opt/map-event-handler map-event-handler}
    :middleware (cljfx/wrap-map-desc root)))

(cljfx/mount-app *state app)
