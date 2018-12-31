(ns e03-map-event-handler
  (:require [cljfx.api :as cljfx])
  (:import [javafx.scene.input KeyCode]))

(def *state
  (atom {:fx/type :stage
         :always-on-top true
         :style :transparent
         :showing true
         :scene {:fx/type :scene
                 :fill :transparent
                 :on-key-pressed {:event/type :event/scene-key-press}
                 :stylesheets #{"styles.css"}
                 :root {:fx/type :v-box
                        :children [{:fx/type :label :text "Hi! What's your name?"}
                                   {:fx/type :text-field}]}}}))

(defn map-event-handler [e]
  (when (and (= :event/scene-key-press (:event/type e))
             (= KeyCode/ESCAPE (-> e :fx/event :code)))
    (reset! *state nil)))

(def app
  (cljfx/create-app
    :opts {:fx.opt/map-event-handler map-event-handler}))

(cljfx/mount-app *state app)