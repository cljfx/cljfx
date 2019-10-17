(ns cljfx.lifecycle-test
  (:require [clojure.test :refer :all]
            [testit.core :refer :all]
            [cljfx.api :as fx])
  (:import [javafx.scene.control Label]))

(deftest env-test
  (let [label (fn [{:keys [a b]}]
                {:fx/type :label
                 :text (str :a " " a ", " :b " " b)})
        c-1 (fx/create-component {:fx/type fx/ext-set-env
                                  :env {:a 1 :b 2}
                                  :desc {:fx/type fx/ext-get-env
                                         :env [:a :b]
                                         :desc {:fx/type label}}})
        ^Label i-1 (fx/instance c-1)
        _ (fact (.getText i-1) => ":a 1, :b 2")
        c-2 (fx/advance-component c-1 {:fx/type fx/ext-set-env
                                       :env {:a 2 :b 2}
                                       :desc {:fx/type fx/ext-get-env
                                              :env [:a :b]
                                              :desc {:fx/type label}}})
        ^Label i-2 (fx/instance c-2)
        _ (fact (.getText i-2) => ":a 2, :b 2")
        c-3 (fx/advance-component c-2 {:fx/type fx/ext-set-env
                                       :env {:x 1 :y 2}
                                       :desc {:fx/type fx/ext-get-env
                                              :env {:x :a
                                                    :y :b}
                                              :desc {:fx/type label}}})
        ^Label i-3 (fx/instance c-3)
        _ (fact (.getText i-3) => ":a 1, :b 2")
        _ (fact (= i-1 i-2 i-3) => true)]))
