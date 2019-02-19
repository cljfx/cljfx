(ns e09-todo-app
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.input KeyCode KeyEvent]))

(def *state
  (atom {:typed-text ""
         :by-id {0 {:id 0
                    :text "Buy milk"
                    :done true}
                 1 {:id 1
                    :text "Buy socks"
                    :done false}}}))

(defn todo-view [{:keys [text id done]}]
  {:fx/type :h-box
   :spacing 5
   :padding 5
   :children [{:fx/type :check-box
               :selected done
               :on-selected-changed {:event/type ::set-done :id id}}
              {:fx/type :label
               :style {:-fx-text-fill (if done :grey :black)}
               :text text}]})

(defn root [{:keys [by-id typed-text]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :pref-width 300
                  :pref-height 400
                  :children [{:fx/type :scroll-pane
                              :v-box/vgrow :always
                              :fit-to-width true
                              :content {:fx/type :v-box
                                        :children (->> by-id
                                                       vals
                                                       (sort-by (juxt :done :id))
                                                       (map #(assoc %
                                                               :fx/type todo-view
                                                               :fx/key (:id %))))}}
                             {:fx/type :text-field
                              :v-box/margin 5
                              :text typed-text
                              :prompt-text "Add new todo and press ENTER"
                              :on-text-changed {:event/type ::type}
                              :on-key-pressed {:event/type ::press}}]}}})

(defn map-event-handler [event]
  (case (:event/type event)
    ::set-done (swap! *state assoc-in [:by-id (:id event) :done] (:fx/event event))
    ::type (swap! *state assoc :typed-text (:fx/event event))
    ::press (when (= KeyCode/ENTER (.getCode ^KeyEvent (:fx/event event)))
              (swap! *state #(-> %
                                 (assoc :typed-text "")
                                 (assoc-in [:by-id (count (:by-id %))]
                                           {:id (count (:by-id %))
                                            :text (:typed-text %)
                                            :done false}))))
    nil))

(fx/mount-renderer
  *state
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler map-event-handler}))
