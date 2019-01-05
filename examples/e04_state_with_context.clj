(ns e04-state-with-context
  (:require [cljfx.api :as fx]))

(def *state
  (atom {:first-name "Vlad"
         :last-name "Protsenko"}))

(defn text-input [{:keys [fx/context label-text key]}]
  {:fx/type :v-box
   :children [{:fx/type :label
               :text label-text}
              {:fx/type :text-field
               :on-text-changed #(swap! *state assoc key %)
               :text (get context key)}]})

(defn root [{:keys [fx/context]}]
  (let [{:keys [first-name last-name]} context]
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :children [{:fx/type :label
                                :text (str "You are " first-name " " last-name "!")}
                               {:fx/type text-input
                                :label-text "First Name"
                                :key :first-name}
                               {:fx/type text-input
                                :label-text "Last Name"
                                :key :last-name}]}}}))

(def app
  (fx/create-app
    :middleware (comp
                  fx/wrap-set-desc-as-context
                  (fx/wrap-map-desc (constantly {:fx/type root})))
    :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(fx/mount-app *state app)