(ns cljfx.event-handlers-test
  (:require [cljfx.api :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]
            [clojure.test :refer :all])
  (:import [javafx.beans.value ChangeListener]
           [javafx.scene.control TextField]))

(defn ext-tracking-text-changed-listeners [f]
  (fx/make-ext-with-props
    {:on-text-changed (prop/make
                        (mutator/adder-remover
                          (fn [^TextField instance ^ChangeListener value]
                            (f :add-listener)
                            (.addListener (.textProperty instance) value))
                          (fn [^TextField instance ^ChangeListener value]
                            (f :remove-listener)
                            (.removeListener (.textProperty instance) value)))
                        lifecycle/change-listener)}))

(deftest test-swapping-handlers
  (let [events (atom [])
        ext (ext-tracking-text-changed-listeners #(swap! events conj %))
        opts {:fx.opt/map-event-handler #(swap! events conj %)}
        c1 (fx/create-component
             {:fx/type ext
              :props {:on-text-changed #(swap! events conj [:first %])}
              :desc {:fx/type :text-field
                     :text "text"}}
             opts)
        ^TextField inst (fx/instance c1)
        _ (.setText inst "text 2")
        ;; advance to different fn listener: same JavaFX listener, new events
        c2 (fx/advance-component
             c1
             {:fx/type ext
              :props {:on-text-changed #(swap! events conj [:second %])}
              :desc {:fx/type :text-field
                     :text "text"}}
             opts)
        _ (.setText inst "text 3")
        ;; advance to map event listener: changes the listener
        c3 (fx/advance-component
             c2
             {:fx/type ext
              :props {:on-text-changed {:map-event-handler :first}}
              :desc {:fx/type :text-field
                     :text "text"}}
             opts)
        _ (.setText inst "text 4")
        ;; advance to another map event listener: same JavaFX listener, new events
        c4 (fx/advance-component
             c3
             {:fx/type ext
              :props {:on-text-changed {:map-event-handler :second}}
              :desc {:fx/type :text-field
                     :text "text"}}
             opts)
        _ (.setText inst "text 5")]
    (is (= [;; initial listener
            :add-listener
            ;; first fn listener
            [:first "text 2"]
            ;; second fn listener, same javafx listener
            [:second "text 3"]
            ;; replace javafx listener when switch from fn to map listeners
            :remove-listener
            :add-listener
            ;; first map listener
            {:map-event-handler :first
             :fx/event "text 4"}
            ;; second map listener, same javafx listener
            {:map-event-handler :second
             :fx/event "text 5"}]
           @events))))

(deftest test-swapping-else-handlers
  (let [handler (reify ChangeListener
                  (changed [_ _ _ _]))
        c1 (fx/create-component
             {:fx/type :text-field
              :on-text-changed handler})]
    (try
      (fx/advance-component
        c1
        {:fx/type :text-field
         :on-text-changed handler})
      (catch Exception e
        (is false (str e))))))