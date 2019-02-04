(ns e17-dialogs
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.control DialogEvent Dialog ButtonType ButtonBar$ButtonData]))

(set! *warn-on-reflection* true)

(def *state
  (atom :select-action))

(defn window [{:keys [nuke-launch-stage]}]
  {:fx/type :stage
   :showing (= nuke-launch-stage :select-action)
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

(defn dialog [{:keys [nuke-launch-stage]}]
  {:fx/type :dialog
   :showing (= nuke-launch-stage :confirm)
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

(defn alert [{:keys [nuke-launch-stage]}]
  {:fx/type :alert
   :alert-type :warning
   :showing (= nuke-launch-stage :confirmed)
   :on-close-request (fn [^DialogEvent e]
                       (when (nil? (.getResult ^Dialog (.getSource e)))
                         (.consume e)))
   :on-hidden (fn [_]
                (reset! *state :choose-vault))
   :header-text "Nuke launch inevitable"
   :content-text "Please press Yes"
   :button-types [:yes]})

(defn choice-dialog [{:keys [nuke-launch-stage]}]
  {:fx/type :choice-dialog
   :showing (= nuke-launch-stage :choose-vault)
   :on-close-request (fn [^DialogEvent e]
                       (when (nil? (.getResult ^Dialog (.getSource e)))
                         (.consume e)))
   :on-hidden (fn [_]
                (reset! *state :final-notes))
   :items [{:id :vaults/v8}
           {:id :vaults/v13}
           {:id :vaults/v15}]})

(defn text-input-dialog [{:keys [nuke-launch-stage]}]
  {:fx/type :text-input-dialog
   :showing (= nuke-launch-stage :final-notes)
   :content-text "Final notes?"
   :on-hidden (fn [^DialogEvent e]
                (let [result (.getResult ^Dialog (.getSource e))]
                  (println (format "Bye! Your final words were \"%s\"" result))
                  (reset! *state :the-end)))})

(def app
  (fx/create-app
    :middleware (comp
                  (fx/wrap-map-desc (fn [nuke-launch-stage]
                                      [{:fx/type window
                                        :nuke-launch-stage nuke-launch-stage}
                                       {:fx/type dialog
                                        :nuke-launch-stage nuke-launch-stage}
                                       {:fx/type alert
                                        :nuke-launch-stage nuke-launch-stage}
                                       {:fx/type choice-dialog
                                        :nuke-launch-stage nuke-launch-stage}
                                       {:fx/type text-input-dialog
                                        :nuke-launch-stage nuke-launch-stage}]))
                  fx/wrap-many)))

(fx/mount-app *state app)
