(ns e41-ext-watcher-concurrency
  (:require [cljfx.api :as fx]))

;; this example is similar to e24-concurrency, but uses ext-watcher instead.

(defn- reactive-view [{:keys [value]}]
  {:fx/type :v-box
   :padding 50
   :spacing 10
   :children [{:fx/type :label
               :text (str "Count: " value)}
              {:fx/type :button
               :text "Responsive button!"}]})

(let [state (atom 0)]
  (fx/on-fx-thread
    (fx/create-component
      {:fx/type :stage
       :showing true
       :scene {:fx/type :scene
               :root {:fx/type fx/ext-watcher
                      :ref state
                      :desc {:fx/type reactive-view}}}}))
  ;; When the state is flooded with changes, cljfx stays responsive by limiting
  ;; component updates to at most 1 per JavaFX frame
  (dotimes [_ 16]
    (.start (Thread. #(dotimes [_ 1000000]
                        (swap! state inc))))))