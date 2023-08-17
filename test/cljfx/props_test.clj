(ns cljfx.props-test
  (:require [clojure.test :refer :all]
            [cljfx.api :as fx])
  (:import [javafx.scene.control TextField]))

(deftest node-props-test
  (let [^TextField tf (fx/instance (fx/create-component {:fx/type :text-field
                                                         :user-data ::user-data}))]
    (is (= ::user-data (.getUserData tf)))))