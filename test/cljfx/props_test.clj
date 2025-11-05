(ns cljfx.props-test
  (:require [clojure.test :refer :all]
            [cljfx.api :as fx]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TextField]))

(deftest node-props-test
  (let [^TextField tf (fx/instance (fx/create-component {:fx/type :text-field
                                                         :user-data ::user-data}))]
    (is (= ::user-data (.getUserData tf)))))

(deftest custom-prop-as-key
  (let [p (prop/make (mutator/setter #(.setUserData ^TextField %1 %2)) lifecycle/scalar)
        c (fx/create-component {:fx/type :text-field p :via-custom-prop})]
    (is (= :via-custom-prop (.getUserData ^TextField (fx/instance c))))))
