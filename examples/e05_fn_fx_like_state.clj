(ns e05-fn-fx-like-state
  (:require [cljfx.api :as cljfx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [label value key]
  [:v-box
   [:label label]
   [:text-field
    {:on-text-changed #(swap! *state assoc key %)
     :text value}]])

(defn root [{:keys [first-name last-name]}]
  [:stage
   {:showing true}
   [:scene
    [:v-box
     [:label (str "You are " first-name " " last-name "!")]
     [text-input "First Name" first-name :first-name]
     [text-input "Last Name" last-name :last-name]]]])

(def app
  (cljfx/create-app
    (cljfx/wrap-map-value root)))

(cljfx/mount-app *state app)