(ns e18-pure-event-handling.subs
  (:require [cljfx.api :as fx]
            [clojure.string :as str]))

(defn current-request-id [context]
  (peek (fx/sub context :history)))

(defn response-by-request-id [context request-id]
  (get (fx/sub context :request-id->response) request-id))

(defn context-type [context request-id]
  (-> (fx/sub context response-by-request-id request-id)
      :response
      :headers
      (get "Content-Type")
      (str/split #";\s*")
      first))

(defn body [context request-id]
  (:body (:response (fx/sub context response-by-request-id request-id))))

(defn current-response [context]
  (let [id (fx/sub context current-request-id)]
    (fx/sub context response-by-request-id id)))

(defn history-empty? [context]
  (empty? (fx/sub context :history)))
