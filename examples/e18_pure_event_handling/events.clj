(ns e18-pure-event-handling.events
  (:require [cljfx.api :as fx]
            [e18-pure-event-handling.subs :as subs])
  (:import [java.util UUID]
           [javafx.scene.input KeyCode KeyEvent]))

(defmulti event-handler :event/type)

(defmethod event-handler :default [event]
  (prn event))

(defmethod event-handler ::type-url [{:keys [fx/context fx/event]}]
  {:context (fx/swap-context context assoc :typed-url event)})

(defmethod event-handler ::key-press-url [{:keys [fx/context ^KeyEvent fx/event]}]
  (let [current-url (:url (fx/sub-ctx context subs/current-response))
        url (fx/sub-val context :typed-url)]
    (when (and (= KeyCode/ENTER (.getCode event))
               (not= url current-url))
      {:dispatch {:event/type ::open-url :url url}})))

(defmethod event-handler ::open-url [{:keys [fx/context url]}]
  (let [request-id (UUID/randomUUID)]
    {:context (fx/swap-context
                context
                (fn [m]
                  (-> m
                      (assoc :typed-url url)
                      (assoc-in [:request-id->response request-id] {:result :pending :url url})
                      (update :history conj request-id))))
     :http {:method :get
            :url url
            :on-response {:event/type ::on-response
                          :request-id request-id
                          :result :success}
            :on-exception {:event/type ::on-response
                           :request-id request-id
                           :result :failure}}}))

(defmethod event-handler ::on-response [{:keys [fx/context request-id result response exception]}]
  {:context (fx/swap-context context
                             update-in [:request-id->response request-id]
                             #(cond-> %
                                :always (assoc :result result)
                                (= :success result) (assoc :response response)
                                (= :failure result) (assoc :exception exception)))})

(defmethod event-handler ::go-back [{:keys [fx/context]}]
  (let [new-active-request-id (peek (pop (fx/sub-val context :history)))
        url (:url (fx/sub-ctx context subs/response-by-request-id new-active-request-id))]
    {:context (fx/swap-context context #(-> %
                                            (update :history pop)
                                            (assoc :typed-url url)))}))
