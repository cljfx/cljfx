(ns e18-pure-event-handling
  (:require [clj-http.client :as http]
            [cljfx.api :as fx]
            [e18-pure-event-handling.events :as events]
            [e18-pure-event-handling.views :as views]))

(def *state
  (atom
    (fx/create-context
      {:typed-url ""
       :request-id->response {}
       :history []})))

(def app-event-handler
  (-> events/event-handler
      (fx/wrap-co-effects
        {:fx/context (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
        {:context (fx/make-reset-effect *state)
         :dispatch fx/dispatch-effect
         :http (fn [v dispatch!]
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
                     (dispatch! (assoc (:on-exception v) :exception e)))))})
      (fx/wrap-async)))

(def app
  (fx/create-app
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_] {:fx/type views/root})))
    :opts {:fx.opt/map-event-handler app-event-handler
           :fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(fx/mount-app *state app)

(app-event-handler {:event/type ::events/open-url :url "https://github.com/cljfx/cljfx/"})
