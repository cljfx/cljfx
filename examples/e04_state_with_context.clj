(ns e04-state-with-context
  (:require [cljfx.api :as cljfx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [state label-text key]
  [:v-box
   {:children [[:label {:text label-text}]
               [:text-field
                {:on-text-changed #(swap! *state assoc key %)
                 :text (get state key)}]]}])

(defn root [state]
  [:stage
   {:showing true
    :scene
    [:scene
     {:root
      [:v-box
       {:children
        [[:label {:text (str "You are " (:first-name state) " " (:last-name state) "!")}]
         [text-input "First Name" :first-name]
         [text-input "Last Name" :last-name]]}]}]}])

(def app
  (cljfx/create-app
    :middleware (comp
                  cljfx/wrap-set-desc-as-context
                  (cljfx/wrap-map-desc (constantly [root])))
    :opts {:cljfx.opt/tag->lifecycle #(or (cljfx/fx-tag->lifecycle %)
                                          (cljfx/fn-tag->lifecycle-with-context %))}))

(cljfx/mount-app *state app)