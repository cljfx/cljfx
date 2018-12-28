(ns e04-exposed-state
  (:require [cljfx.api :as cljfx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [state label-text key]
  [:v-box
   [:label label-text]
   [:text-field
    {:on-text-changed #(swap! *state assoc key %)
     :text (get state key)}]])

(defn root [state]
  [:stage {:showing true}
   [:scene
    [:v-box
     [:label (str "You are " (:first-name state) " " (:last-name state) "!")]
     [text-input "First Name" :first-name]
     [text-input "Last Name" :last-name]]]])

(def app
  (cljfx/create-app
    :middleware (comp
                  (cljfx/wrap-expose-value)
                  (cljfx/wrap-map-value (constantly [root])))
    :opts {:cljfx.opt/tag->lifecycle #(or (cljfx/fx-tag->lifecycle %)
                                          (cljfx/fn-tag->exposed-lifecycle %))}))

(cljfx/mount-app *state app)