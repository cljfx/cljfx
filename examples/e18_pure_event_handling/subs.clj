(ns e18-pure-event-handling.subs
  (:require [cljfx.api :as fx]
            [clojure.string :as str]))

(defn current-request-id [context]
  (peek (fx/sub context :history)))

(defn context-type [context request-id]
  (-> (fx/sub context :request-id->response request-id :response :headers)
      (get "Content-Type")
      (str/split #";\s*")
      first))

(defn body [context request-id]
  (fx/sub context :request-id->response request-id :response :body))

(defn current-response [context]
  (let [id (fx/sub context current-request-id)]
    (fx/sub context :request-id->response id)))

(defn history-empty? [context]
  (empty? (fx/sub context :history)))
