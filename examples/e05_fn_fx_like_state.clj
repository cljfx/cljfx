(ns e05-fn-fx-like-state
  (:require [cljfx.api :as cljfx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [label value key]
  [:v-box
   {:children [[:label {:text label}]
               [:text-field
                {:on-text-changed {:key key}
                 :text value}]]}])

(defn root [{:keys [first-name last-name]}]
  [:stage
   {:showing true
    :scene
    [:scene
     {:root
      [:v-box
       {:children [(if (empty? (str first-name last-name))
                     [:v-box
                      {:effect [:effect/drop-shadow]
                       :children [[:label {:text "You are very mysterious!"}]
                                  [:label {:text "Please, introduce yourself:"}]]}]
                     [:label {:text (str "You are " first-name " " last-name "!")}])
                   [text-input "First Name" first-name :first-name]
                   [text-input "Last Name" last-name :last-name]]}]}]}])

(defn map-event-handler [event]
  (swap! *state assoc (:key event) (:cljfx/event event)))

(def app
  (cljfx/create-app
    :middleware (cljfx/wrap-map-desc root)
    :opts {:cljfx.opt/map-event-handler map-event-handler}))

(cljfx/mount-app *state app)