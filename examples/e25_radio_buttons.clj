(ns e25-radio-buttons
  (:require [cljfx.api :as fx]))

(def *option
  (atom {:option :a}))

(defn option-view [{:keys [value selected-value]}]
  {:fx/type :radio-button
   :selected (= value selected-value)
   :text (str value)
   :on-action {:value value}})

(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler
           (fn [{:keys [value]}]
             (swap! *option update :option #(if (= value %) nil value)))}
    :middleware (fx/wrap-map-desc
                  (fn [{:keys [option]}]
                    {:fx/type :stage
                     :showing true
                     :scene {:fx/type :scene
                             :root {:fx/type :h-box
                                    :padding 20
                                    :spacing 10
                                    :children [{:fx/type option-view
                                                :value :a
                                                :selected-value option}
                                               {:fx/type option-view
                                                :value :b
                                                :selected-value option}
                                               {:fx/type option-view
                                                :value :c
                                                :selected-value option}]}}}))))

(fx/mount-renderer *option renderer)
