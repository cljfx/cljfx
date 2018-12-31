(ns e02-fn
  (:require [cljfx.api :as cljfx]))

(defn text-input [label]
  [:v-box
   {:children [[:label {:text label}]
               [:text-field]]}])

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage
     {:showing true
      :scene [:scene
              {:root [:v-box
                      {:children [[text-input "First Name"]
                                  [text-input "Last Name"]]}]}]}]))