(ns cljfx.local-state-test
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
             {:fx/type fx/ext-state
              :initial-state "text"
              :desc {:fx/type text-field-view
                     :key 1}})
        ;; swap state
        ^TextField tf (fx/instance c1)
        _ (.setText tf "text 2")
        ;; wait for re-render
        _ @(fx/run-later)
        ;; advance: change desc
        c2 (fx/advance-component
             c1
             {:fx/type fx/ext-state
              :initial-state "text"
              :desc {:fx/type text-field-view
                     :key 2}})
        ;; advance: change initial state
        _ (fx/advance-component
            c2
            {:fx/type fx/ext-state
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

(binding [*test-out* *out*]
  (let [refresh-events (atom [])
        text-field-view (fn [{:keys [text prompt-text swap-text] :as props}]
                          (swap! refresh-events conj props)
                          {:fx/type :text-field
                           :text (or text "")
                           :prompt-text (or prompt-text "")
                           :on-text-changed #(swap-text (constantly %))})
        c (fx/create-component
            {:fx/type fx/ext-state
             :initial-state "foo"
             :key :text
             :swap-key :swap-text
             :desc {:fx/type text-field-view}})
        ^TextField text-field (fx/instance c)
        _ (is (= "foo" (.getText text-field)))
        _ (is (= "" (.getPromptText text-field)))
        _ (.setText text-field "bar")
        _ @(fx/run-later "Waiting...")
        _ (is (= "bar" (.getText text-field)))
        _ (is (= "" (.getPromptText text-field)))
        c (fx/advance-component
            c
            {:fx/type fx/ext-state
             :initial-state "foo"
             :key :prompt-text
             :swap-key :swap-text
             :desc {:fx/type text-field-view}})
        _ (is (= "" (.getText text-field)))
        _ (is (= "bar" (.getPromptText text-field)))
        _ (fx/delete-component c)
        swap-text (:swap-text (first @refresh-events))]
    (is (= [;; initial render
            {:text "foo"
             :swap-text swap-text}
            ;; modification
            {:text "bar"
             :swap-text swap-text}
            ;; advance to new key
            {:prompt-text "bar"
             :swap-text swap-text}]
           @refresh-events))))
