(ns e03-map-event-handler
  (:require [cljfx.api :as cljfx])
  (:import [javafx.scene.input KeyCode]))

(def *state
  (atom [:stage
         {:always-on-top true
          :style :transparent
          :showing true}
         [:scene
          {:fill :transparent
           :on-key-pressed {:event :event/scene-key-press}
           :stylesheets #{"styles.css"}}
          [:v-box
           [:label "Hi! What's your name?"]
           [:text-field]]]]))

(def app
  (cljfx/create-app
    (cljfx/wrap-add-map-event-handler
      #(when (and (= :event/scene-key-press (:event %))
                  (= KeyCode/ESCAPE (-> % :cljfx/event :code)))
         (reset! *state nil)))))

(cljfx/mount-app *state app)