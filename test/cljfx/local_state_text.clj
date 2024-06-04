(ns cljfx.local-state-text
  (:require [cljfx.api :as fx]
            [clojure.test :refer :all])
  (:import [javafx.scene.control TextField]))

(deftest test-lifecycle-semantics
 (let [refresh-events (atom [])
       text-field-view (fn [{:keys [state swap-state key]}]
                         (swap! refresh-events conj [state key swap-state])
                         {:fx/type :text-field
                          :text state
                          :on-text-changed #(swap-state (constantly %))})
       ;; initial creation
       c1 (fx/create-component
            {:fx/type fx/ext-local-state
             :initial-state "text"
             :desc {:fx/type text-field-view
                    :key 1}})
       ;; swap state
       ^TextField tf (fx/instance c1)
       _ (.setText tf "text 2")
       ;; wait for re-render
       _ @(fx/on-fx-thread)
       ;; advance: change desc
       c2 (fx/advance-component
            c1
            {:fx/type fx/ext-local-state
             :initial-state "text"
             :desc {:fx/type text-field-view
                    :key 2}})
       ;; advance: change initial state
       _ (fx/advance-component
           c2
           {:fx/type fx/ext-local-state
            :initial-state "new initial text"
            :desc {:fx/type text-field-view
                   :key 2}})]
   (let [events @refresh-events
         first-swap-state (last (first events))
         last-swap-state (last (last events))]
     (is (= [;; tuple is state+desc-key+swap-state-fn
             ;; initial creation
             ["text" 1 first-swap-state]
             ;; swap state by set text
             ["text 2" 1 first-swap-state]
             ;; advance: change desc (key: 1 -> 2)
             ["text 2" 2 first-swap-state]
             ;; advance: change initial state, causes swap-state to change too
             ["new initial text" 2 last-swap-state]]
            events)))))
