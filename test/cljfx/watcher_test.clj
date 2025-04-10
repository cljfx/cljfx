(ns cljfx.watcher-test
  (:require [cljfx.api :as fx]
            [clojure.test :refer :all])
  (:import [javafx.scene.control Label TextField]))

(deftest dedupe-on-fx-thread
  (let [state (atom 0)
        refresh-counter (atom 0)]
    (fx/instance
      (fx/create-component
        {:fx/type fx/ext-watcher
         :ref state
         :desc {:fx/type (fn [{:keys [value key]}]
                           (swap! refresh-counter inc)
                           {:fx/type :label
                            :text (str value key)})
                :key :test}}))
    (dotimes [_ 100000]
      (swap! state inc))
    (is (< @refresh-counter 1000))))

(deftest key-test
  (let [state (atom "Foo")
        ^Label label (fx/instance
                       (fx/create-component
                         {:fx/type fx/ext-watcher
                          :ref state
                          :key :text
                          :desc {:fx/type :label}}))]
    (is (= "Foo" (.getText label)))
    (reset! state "Bar")
    (is (= "Bar" @(fx/run-later (.getText label))))))

(deftest key-advance-test
  (let [state (atom "Some text")
        c1 (fx/create-component
             {:fx/type fx/ext-watcher
              :ref state
              :key :text
              :desc {:fx/type :text-field}})
        ^TextField text-field (fx/instance c1)
        _ (is (= "Some text" (.getText text-field)))
        c2 (fx/advance-component
             c1
             {:fx/type fx/ext-watcher
              :ref state
              :key :prompt-text
              :desc {:fx/type :text-field}})
        _ (is (nil? (.getText text-field)))
        _ (is (= "Some text" (.getPromptText text-field)))
        _ (reset! state "Another text")
        _ @(fx/run-later "Waiting...")
        _ @(fx/run-later "Waiting...")
        _ @(fx/run-later "Waiting...")
        _ @(fx/run-later "Waiting...")
        _ (is (= "Another text" (.getPromptText text-field)))
        _ (is (nil? (.getText text-field)))]
    (fx/delete-component c2)
    (is (= {} (.getWatches state)))))
