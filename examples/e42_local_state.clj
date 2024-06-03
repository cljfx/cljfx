(ns e42-local-state
  (:require [cljfx.api :as fx]))

(defn- text-field-view [{:keys [state swap-state key]}]
  {:fx/type :text-field
   :text (get state key)
   :on-text-changed #(swap-state assoc key %)})

(defn- form-view [{:keys [state swap-state]}]
  {:fx/type :v-box
   :padding 20
   :spacing 10
   :children [{:fx/type text-field-view :state state :swap-state swap-state :key :first-name}
              {:fx/type text-field-view :state state :swap-state swap-state :key :last-name}
              {:fx/type :button
               :text "Print greeting"
               :on-action (fn [_]
                            (println (str "Hello, " (:first-name state) " " (:last-name state) "!")))}]})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type fx/ext-local-state
                    :initial-state {:first-name "Vlops"
                                    :last-name "Props"}
                    :desc {:fx/type form-view}}}}))
