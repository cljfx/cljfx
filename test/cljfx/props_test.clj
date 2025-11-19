(ns cljfx.props-test
  (:require [clojure.test :refer :all]
            [cljfx.api :as fx]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.beans.value ChangeListener]
           [javafx.scene.control TextField]))

(set! *warn-on-reflection* true)

(deftest node-props-test
  (let [^TextField tf (fx/instance (fx/create-component {:fx/type :text-field
                                                         :user-data ::user-data}))]
    (is (= ::user-data (.getUserData tf)))))

(deftest custom-prop-as-key
  (let [p (fx/make-prop (mutator/setter #(.setUserData ^TextField %1 %2)) lifecycle/scalar)
        c (fx/create-component {:fx/type :text-field p :via-custom-prop})]
    (is (= :via-custom-prop (.getUserData ^TextField (fx/instance c))))))

(deftest meta-test
  (let [p1 (fx/make-prop mutator/forbidden lifecycle/scalar)
        p2 (vary-meta p1 assoc :meta true)]
    (is (nil? (meta p1)))
    (is (= {:meta true} (meta p2)))
    (is (= p1 p2))
    (is (.equals ^Object p1 p2))
    (is (= {:cljfx/id 'number-prop :cljfx/prop {:type :number} :meta true}
           (meta (prop/annotate p2 'number-prop {:type :number}))))))

(deftest binding-test
  (binding [lifecycle/*in-progress?* false]
    (testing "bind"
      (let [events (atom [])
            p (fx/make-binding-prop
                (fn [^TextField text-field cb]
                  (swap! events conj [:bind])
                  (let [change-listener (reify ChangeListener
                                          (changed [_ _ _ v]
                                            (cb v)))
                        text-property (.textProperty text-field)]
                    (.addListener text-property change-listener)
                    #(do
                       (swap! events conj [:unbind])
                       (.removeListener text-property change-listener))))
                lifecycle/event-handler)
            cb1 #(swap! events conj [:callback 1 %])
            ;; create => bind
            c (fx/create-component {:fx/type :text-field p cb1})
            ;; change text => callback 1
            _ (.setText ^TextField (fx/instance c) "a")
            ;; same desc: no change
            c (fx/advance-component c {:fx/type :text-field p cb1})
            cb2 #(swap! events conj [:callback 2 %])
            ;; event handler absorbs the changed function: no change
            c (fx/advance-component c {:fx/type :text-field p cb2})
            ;; set text => callback 2
            _ (.setText ^TextField (fx/instance c) "b")
            ;; prop removed => unbind
            c (fx/advance-component c {:fx/type :text-field})
            ;; set text does nothing (no listeners)
            _ (.setText ^TextField (fx/instance c) "b")
            ;; bind a new one => bind
            c (fx/advance-component c {:fx/type :text-field p cb2})
            ;; delete doesn't unbind: the whole tree is disposed
            _ (fx/delete-component c)]
        (is (= [[:bind]
                [:callback 1 "a"]
                [:callback 2 "b"]
                [:unbind]
                [:bind]]
               @events))))
    (testing "replace"
      (let [events (atom [])
            p (fx/make-binding-prop
                (fn [_ value]
                  (swap! events conj [:bind value])
                  #(swap! events conj [:unbind value]))
                lifecycle/scalar)
            ;; create => bind
            c (fx/create-component {:fx/type :text-field p ::a})
            ;; advance with new value => unbind old binding, bind new one
            c (fx/advance-component c {:fx/type :text-field p ::b})
            ;; delete doesn't unbind: the whole tree is disposed
            _ (fx/delete-component c)]
        (is (= [[:bind ::a]
                [:unbind ::a]
                [:bind ::b]]
               @events))))))
