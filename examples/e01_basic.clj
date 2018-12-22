(ns e01-basic
  (:require [cljfx.api :as cljfx]))

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage
     {:showing true
      :always-on-top true
      :style :transparent}
     [:scene
      {:fill :transparent
       :stylesheets #{"styles.css"}}
      [:v-box
       [:label
        {:effect [:effect/drop-shadow {:radius 1 :offset-y 2}]
         :tooltip [:tooltip "I am a tooltip!"]}
        "Hi! What's your name?"]
       [:text-field]]]]))
