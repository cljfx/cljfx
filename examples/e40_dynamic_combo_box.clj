(ns e40-dynamic-combo-box
  (:require [cljfx.api :as fx]))

(def *state
  (atom
    {::value 1
     ::items [0 1 2 3]}))

(defn set-value! [x]
  (swap! *state assoc ::value x))

(defn add-item! [_]
  (swap! *state update ::items #(conj % (count %))))

(defn view [{::keys [value items]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :combo-box
                              :value value
                              :on-value-changed set-value!
                              :items items}
                             {:fx/type :button
                              :text "Add item"
                              :on-action add-item!}]}}})

(fx/mount-renderer *state
  (fx/create-renderer :middleware (fx/wrap-map-desc view)))

