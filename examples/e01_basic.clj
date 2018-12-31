(ns e01-basic
  (:require [cljfx.api :as cljfx]))

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage
     {:showing true
      :always-on-top true
      :style :transparent
      :scene [:scene
              {:fill :transparent
               :stylesheets #{"styles.css"}
               :root [:v-box
                      {:children [[:label
                                   {:effect [:effect/drop-shadow
                                             {:radius 1 :offset-y 2}]
                                    :tooltip [:tooltip
                                              {:text "I am a tooltip!"}]
                                    :text "Hi! What's your name?"}]
                                  [:text-field]]}]}]}]))
