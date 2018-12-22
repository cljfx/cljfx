(ns e02-fn
  (:require [cljfx.api :as cljfx]))

(defn text-input [label]
  [:v-box
   [:label label]
   [:text-field]])

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage {:showing true}
     [:scene
      [:v-box
       [text-input "First Name"]
       [text-input "Last Name"]]]]))