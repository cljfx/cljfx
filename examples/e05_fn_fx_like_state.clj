(ns e05-fn-fx-like-state
  (:require [cljfx.api :as cljfx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [label value key]
  [:v-box
   [:label label]
   [:text-field
    {:on-text-changed {:key key}
     :text value}]])

(defn root [{:keys [first-name last-name]}]
  [:stage {:showing true}
   [:scene
    [:v-box
     (if (empty? (str first-name last-name))
       [:v-box {:effect [:effect/drop-shadow]}
        [:label "You are very mysterious!"]
        [:label "Please, introduce yourself:"]]
       [:label (str "You are " first-name " " last-name "!")])
     [text-input "First Name" first-name :first-name]
     [text-input "Last Name" last-name :last-name]]]])

(defn map-event-handler [event]
  (swap! *state assoc (:key event) (:cljfx/event event)))

(def app
  (cljfx/create-app
    :middleware (cljfx/wrap-map-desc root)
    :opts {:cljfx.opt/map-event-handler map-event-handler}))

(cljfx/mount-app *state app)