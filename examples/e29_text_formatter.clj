(ns e29-text-formatter
  (:require [cljfx.api :as fx]))

(def *state
  (atom {:name "Meepledorf"
         :age 41}))

(defn labeled-input [{:keys [label input]}]
  {:fx/type :v-box
   :spacing 5
   :children [{:fx/type :label
               :text label}
              input]})

(defn name-input [{:keys [value]}]
  {:fx/type labeled-input
   :label "Name"
   :input {:fx/type :text-field
           :text-formatter {:fx/type :text-formatter
                            :value-converter :default
                            :value value
                            :on-value-changed #(swap! *state assoc :name %)}}})

(defn age-input [{:keys [value]}]
  {:fx/type labeled-input
   :label "Age"
   :input {:fx/type :text-field
           :text-formatter {:fx/type :text-formatter
                            :value-converter :long
                            :value value
                            :on-value-changed #(swap! *state assoc :age %)}}})

(fx/mount-renderer
  *state
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [{:keys [name age]}]
        {:fx/type :stage
         :showing true
         :width 400
         :scene {:fx/type :scene
                 :root {:fx/type :v-box
                        :padding 40
                        :spacing 20
                        :children [{:fx/type :label
                                    :text (str name ", " age " y.o.")}
                                   {:fx/type name-input
                                    :value name}
                                   {:fx/type age-input
                                    :value age}]}}}))))
