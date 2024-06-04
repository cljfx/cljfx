(ns cljfx.watcher-test
  (:require [cljfx.api :as fx]
            [clojure.test :refer :all]))

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
