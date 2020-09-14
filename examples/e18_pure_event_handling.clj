(ns e18-pure-event-handling
  (:require [clj-http.client :as http]
            [cljfx.api :as fx]
            [e18-pure-event-handling.events :as events]
            [e18-pure-event-handling.views :as views]
            [clojure.core.cache :as cache]))

;; This is an example of simplistic web browser built on top of JavaFX: it can display
;; text, some images and html as a tree of nodes. Events, subscriptions and views are all
;; pure function, and all mutations happen only in this ns

(def *state
  (atom
    (fx/create-context
      {:typed-url ""
       :request-id->response {}
       :history []}
      cache/lru-cache-factory)))

;; Example of a custom effect that performs http request in the background, and notifies
;; application with result as a new event. Since events are just maps, performing http
;; request and specifying result handlers can be done as an effect value, for example:
;; [:http {:method :get
;;         :url "https://github.com/cljfx/cljfx/"
;;         :on-response {:event/type ::on-response :result :success}
;;         :on-exception {:event/type ::on-response :result :failure}}]

(defn http-effect [v dispatch!]
  (try
    (http/request
      (-> v
          (assoc :async? true :as :byte-array)
          (dissoc :on-response :on-exception))
      (fn [response]
        (dispatch! (assoc (:on-response v) :response response)))
      (fn [exception]
        (dispatch! (assoc (:on-exception v) :exception exception))))
    (catch Exception e
      (dispatch! (assoc (:on-exception v) :exception e)))))

(def event-handler
  (-> events/event-handler
      (fx/wrap-co-effects
        {:fx/context (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
        {:context (fx/make-reset-effect *state)
         :dispatch fx/dispatch-effect
         :http http-effect})))

(def renderer
  (fx/create-renderer
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_] {:fx/type views/root})))
    :opts {:fx.opt/map-event-handler event-handler
           :fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(event-handler
  {:event/type ::events/open-url
   :url "https://github.com/cljfx/cljfx/"})

(fx/mount-renderer *state renderer)
