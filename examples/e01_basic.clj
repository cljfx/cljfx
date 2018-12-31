(ns e01-basic
  (:require [cljfx.api :as cljfx]))

(cljfx/on-fx-thread
  (cljfx/create-component
    {:fx/type :stage
     :showing true
     :always-on-top true
     :style :transparent
     :scene {:fx/type :scene
             :fill :transparent
             :stylesheets #{"styles.css"}
             :root {:fx/type :v-box
                    :children [{:fx/type :label
                                :effect {:fx/type :effect/drop-shadow
                                         :radius 1
                                         :offset-y 2}
                                :tooltip {:fx/type :tooltip
                                          :text "I am a tooltip!"}
                                :text "Hi! What's your name?"}
                               {:fx/type :text-field}]}}}))
