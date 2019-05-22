(ns e17-dialogs
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.control DialogEvent Dialog ButtonType ButtonBar$ButtonData]))

(set! *warn-on-reflection* true)

(def *state
  (atom :select-action))

(defn window [_]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :padding 20
                  :spacing 10
                  :children [{:fx/type :label
                              :text "Launch nukes?"}
                             {:fx/type :button
                              :text "Yes"
                              :on-action (fn [_]
                                           (reset! *state :confirm))}]}}})

(defn dialog [_]
  {:fx/type :dialog
   :showing true
   :on-hidden (fn [^DialogEvent e]
                (condp = (.getButtonData ^ButtonType (.getResult ^Dialog (.getSource e)))
                  ButtonBar$ButtonData/NO (reset! *state :select-action)
                  ButtonBar$ButtonData/YES (reset! *state :confirmed)))
   :dialog-pane {:fx/type :dialog-pane
                 :header-text "Nuke launch confirmation dialog™"
                 :content-text "Are you sure?"
                 :expandable-content {:fx/type :label
                                      :text "This action can't be undone."}
                 :button-types [:no :yes]}})

(defn alert [_]
  {:fx/type :alert
   :alert-type :warning
   :showing true
   :on-close-request (fn [^DialogEvent e]
                       (when (nil? (.getResult ^Dialog (.getSource e)))
                         (.consume e)))
   :on-hidden (fn [_]
                (reset! *state :choose-vault))
   :header-text "Nuke launch inevitable"
   :content-text "Please press Yes"
   :button-types [:yes]})

(defn choice-dialog [_]
  {:fx/type :choice-dialog
   :showing true
   :on-close-request (fn [^DialogEvent e]
                       (when (nil? (.getResult ^Dialog (.getSource e)))
                         (.consume e)))
   :on-hidden (fn [_]
                (reset! *state :final-notes))
   :header-text "Please choose vault"
   :items [{:id :vaults/v8}
           {:id :vaults/v13}
           {:id :vaults/v15}]})

(defn text-input-dialog [_]
  {:fx/type :text-input-dialog
   :showing true
   :header-text "Final notes?"
   :on-hidden (fn [^DialogEvent e]
                (let [result (.getResult ^Dialog (.getSource e))]
                  (println (format "Bye! Your final words were \"%s\"" result))
                  (reset! *state :the-end)))})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc
                  (fn [nuke-launch-stage]
                    {:fx/type fx/ext-many
                     :desc (case nuke-launch-stage
                             :select-action [{:fx/type window}]
                             :confirm [{:fx/type dialog}]
                             :confirmed [{:fx/type alert}]
                             :choose-vault [{:fx/type choice-dialog}]
                             :final-notes [{:fx/type text-input-dialog}]
                             [])}))))

(fx/mount-renderer *state renderer)
