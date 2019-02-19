(ns e05-fn-fx-like-state
  (:require [cljfx.api :as fx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [{:keys [label value key]}]
  {:fx/type :v-box
   :children [{:fx/type :label
               :text label}
              {:fx/type :text-field
               :on-text-changed {:key key}
               :text value}]})

(defn root [{:keys [first-name last-name]}]
  {:fx/type :stage
   :showing true
   :scene
   {:fx/type :scene
    :root
    {:fx/type :v-box
     :children [(if (empty? (str first-name last-name))
                  {:fx/type :v-box
                   :effect {:fx/type :drop-shadow}
                   :children [{:fx/type :label :text "You are very mysterious!"}
                              {:fx/type :label :text "Please, introduce yourself:"}]}
                  {:fx/type :label
                   :text (str "You are " first-name " " last-name "!")})
                {:fx/type text-input
                 :label "First Name"
                 :value first-name
                 :key :first-name}
                {:fx/type text-input
                 :label "Last Name"
                 :value last-name
                 :key :last-name}]}}})

(defn map-event-handler [event]
  (swap! *state assoc (:key event) (:fx/event event)))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc root)
    :opts {:fx.opt/map-event-handler map-event-handler}))

(fx/mount-renderer *state renderer)
