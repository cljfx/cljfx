(ns e02-fn
  (:require [cljfx.api :as cljfx]))

(defn text-input [{:keys [label]}]
  {:fx/type :v-box
   :children [{:fx/type :label :text label}
              {:fx/type :text-field}]})

(cljfx/on-fx-thread
  (cljfx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :children [{:fx/type text-input
                                :label "First Name"}
                               {:fx/type text-input
                                :label "Last Name"}]}}}))
