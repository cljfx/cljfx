(ns cljfx.platform-test
  (:require [cljfx.api :as fx]
            [clojure.test :refer :all]))

(defn- deref-result [x]
  (try
    [:returned @x]
    (catch Throwable e
      [:thrown e])))

(deftest run-later-catches-throwable
  (let [error (Error. "run-later error")
        result (fx/run-later (throw error))
        [outcome thrown] (deref-result result)]
    (is (instance? clojure.lang.IDeref result))
    (is (= :thrown outcome))
    (is (identical? error thrown))))

(deftest on-fx-thread-immediate-path-catches-throwable
  (let [error (Error. "on-fx-thread error")
        [result evaluated?]
        @(fx/run-later
           (let [evaluated? (atom false)
                 result (fx/on-fx-thread
                          (reset! evaluated? true)
                          (throw error))]
             [result @evaluated?]))]
    (is evaluated?)
    (is (instance? clojure.lang.IDeref result))
    (let [[outcome thrown] (deref-result result)]
      (is (= :thrown outcome))
      (is (identical? error thrown)))))
