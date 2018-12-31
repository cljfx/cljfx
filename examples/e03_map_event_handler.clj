(ns e03-map-event-handler
  (:require [cljfx.api :as cljfx])
  (:import [javafx.scene.input KeyCode]))

(def *state
  (atom [:stage
         {:always-on-top true
          :style :transparent
          :showing true
          :scene [:scene
                  {:fill :transparent
                   :on-key-pressed {:event/type :event/scene-key-press}
                   :stylesheets #{"styles.css"}
                   :root [:v-box
                          {:children [[:label {:text "Hi! What's your name?"}]
                                      [:text-field]]}]}]}]))

(defn map-event-handler [e]
  (when (and (= :event/scene-key-press (:event/type e))
             (= KeyCode/ESCAPE (-> e :cljfx/event :code)))
    (reset! *state nil)))

(def app
  (cljfx/create-app
    :opts {:cljfx.opt/map-event-handler map-event-handler}))

(cljfx/mount-app *state app)