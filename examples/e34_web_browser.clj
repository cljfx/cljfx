(ns e34-web-browser
  (:require [cljfx.api :as fx]))

(def home-url "http://www.google.com")

(def *state (atom {::partial-url home-url
                   ::current-url home-url}))

(defn top-pane [{:keys [state]}]
  {:fx/type  :h-box
   :spacing  5
   :children [{:fx/type   :button
               :text      "Home"
               :on-action {:event/type ::home}}
              {:fx/type         :text-field
               :h-box/hgrow     :always
               :text            (::partial-url state)
               :on-text-changed {:event/type ::url-change}
               :on-action       {:event/type ::url-complete}}]})

(defn body-pane [{:keys [state]}]
  {:fx/type  :v-box
   :padding  10
   :spacing  10
   :children [{:fx/type top-pane
               :state   state}
              {:fx/type     :web-view
               :pref-height 1000
               :pref-width  1500
               :url         (::current-url state)}]})

(defn root [state]
  {:fx/type :stage
   :showing true
   :title   "Simple web browser"
   :scene   {:fx/type :scene
             :root    {:fx/type body-pane :state state}}})

(defn event-handler [event]
  (case (:event/type event)
    ::home (reset! *state {::partial-url home-url
                           ::current-url home-url})
    ::url-change (swap! *state assoc ::partial-url (:fx/event event))
    ::url-complete (swap! *state assoc ::current-url (::partial-url @*state))))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
