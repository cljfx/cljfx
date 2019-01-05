(ns e02-fn
  (:require [cljfx.api :as fx]))

(defn text-input [{:keys [label]}]
  {:fx/type :v-box
   :children [{:fx/type :label :text label}
              {:fx/type :text-field}]})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :children [{:fx/type text-input
                                :label "First Name"}
                               {:fx/type text-input
                                :label "Last Name"}]}}}))
