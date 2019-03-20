(ns e24-concurrency
  (:require [cljfx.api :as fx]))

(def *state
  (atom 0))

(def renderer
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [i]
        {:fx/type :stage
         :showing true
         :scene {:fx/type :scene
                 :root {:fx/type :v-box
                        :padding 50
                        :spacing 10
                        :children [{:fx/type :label
                                    :text (str "Count: " i)}
                                   {:fx/type :button
                                    :text "Responsive button!"}]}}}))))

(fx/mount-renderer *state renderer)

;; When *state is flooded with changes, cljfx stays responsive by limiting
;; component updates to at most 1 per JavaFX frame

(dotimes [_ 16]
  (.start
    (Thread.
      #(dotimes [_ 1000000]
         (swap! *state inc)))))
