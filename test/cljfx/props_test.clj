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

(deftest meta-test
  (let [p1 (prop/make mutator/forbidden lifecycle/scalar)
        p2 (vary-meta p1 assoc :meta true)]
    (is (nil? (meta p1)))
    (is (= {:meta true} (meta p2)))
    (is (= p1 p2))
    (is (.equals p1 p2))
    (is (= {:cljfx/id 'number-prop :cljfx/prop {:type :number} :meta true}
           (meta (prop/annotate p2 'number-prop {:type :number}))))))
