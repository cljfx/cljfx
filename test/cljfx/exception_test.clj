(ns cljfx.exception-test
  (:require [clojure.test :refer :all]
            [testit.core :refer :all]
            [cljfx.api :as fx]))

(deftest non-existent-key
  (fact
    (fx/create-component {:fx/type :label "non-existent-key" true})
    =throws=>
    (ex-info? "No such prop: \"non-existent-key\"" {:prop "non-existent-key"})))
