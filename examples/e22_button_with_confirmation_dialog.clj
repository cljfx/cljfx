(ns e22-button-with-confirmation-dialog
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache])
  (:import [javafx.scene.control DialogEvent Dialog ButtonBar$ButtonData ButtonType]))

;; Example of generic component that is a button with confirmation dialog
;; Its local state is kept in `:internal` map of global app state. This component is
;; parameterized with state identifier which is a key in that map where such component can
;; put it's state, so having different independent instances of such component requires to
;; provide different state identifiers for such instances

(def *context
  (atom (fx/create-context
          {:showing true
           :internal {}}
          cache/lru-cache-factory)))

;; Events of `button-with-confirmation-dialog` component

(defmulti handle-event :event/type)

(defmethod handle-event ::show-confirmation
  [{:keys [fx/context state-id]}]
  {:context (fx/swap-context context assoc-in [:internal state-id :showing] true)})

(defmethod handle-event ::on-confirmation-dialog-hidden
  [{:keys [fx/context ^DialogEvent fx/event state-id on-confirmed]}]
  (condp = (.getButtonData ^ButtonType (.getResult ^Dialog (.getSource event)))
    ButtonBar$ButtonData/CANCEL_CLOSE
    {:context (fx/swap-context context assoc-in [:internal state-id :showing] false)}

    ButtonBar$ButtonData/OK_DONE
    {:context (fx/swap-context context assoc-in [:internal state-id :showing] false)
     :dispatch on-confirmed}))

;; Independent event that is used as an example `:on-confirm` action

(defmethod handle-event ::quit
  [{:keys [fx/context]}]
  {:context (fx/swap-context context assoc :showing false)})

;; Component itself:
;; - `:state-id` for local state
;; - `:on-confirmed` is a callback event for action that requires confirmation
;; - `:button` and `:dialog-pane` are prop maps that are used to configure views

(defn button-with-confirmation-dialog [{:keys [fx/context
                                               state-id
                                               on-confirmed
                                               button
                                               dialog-pane]}]
  {:fx/type fx/ext-let-refs
   :refs {::dialog {:fx/type :dialog
                    :showing (fx/sub-val context get-in [:internal state-id :showing] false)
                    :on-hidden {:event/type ::on-confirmation-dialog-hidden
                                :state-id state-id
                                :on-confirmed on-confirmed}
                    :dialog-pane (merge {:fx/type :dialog-pane
                                         :button-types [:cancel :ok]}
                                        dialog-pane)}}
   :desc (merge {:fx/type :button
                 :on-action {:event/type ::show-confirmation
                             :state-id state-id}}
                button)})

(defn root-view [{:keys [fx/context]}]
  {:fx/type :stage
   :showing (fx/sub-val context :showing)
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :padding 50
                  :children [{:fx/type button-with-confirmation-dialog
                              :state-id ::main-quit-button
                              :button {:text "Quit"}
                              :dialog-pane {:content-text "Are you sure?"}
                              :on-confirmed {:event/type ::quit}}]}}})

(def app
  (fx/create-app *context
    :event-handler handle-event
    :desc-fn (fn [_]
               {:fx/type root-view})))
