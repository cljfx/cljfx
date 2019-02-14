(ns e18-pure-event-handling
  (:require [cljfx.api :as fx]
            [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Element]
           [javafx.scene.input KeyEvent KeyCode]
           [java.util UUID]))

(def *state
  (atom
    (fx/create-context
      {:typed-url ""
       :request-id->response {}
       :history []})))

;; region subscriptions

(defn current-request-id-sub [context]
  (peek (fx/sub context :history)))

(defn response-by-request-id-sub [context id]
  (get (fx/sub context :request-id->response) id))

(defn current-response [context]
  (let [id (fx/sub context current-request-id-sub)]
    (fx/sub context response-by-request-id-sub id)))

(defn history-empty?-sub [context]
  (empty? (fx/sub context :history)))

;; endregion

;; region events

(defmulti event-handler :event/type)

(defmethod event-handler :default [event]
  (prn event))

(defmethod event-handler ::type-url [{:keys [fx/context fx/event]}]
  {:context (fx/swap-context context assoc :typed-url event)})

(defmethod event-handler ::key-press-url [{:keys [fx/context ^KeyEvent fx/event]}]
  (let [current-url (:url (fx/sub context current-response))
        url (fx/sub context :typed-url)]
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
  (let [new-active-request-id (peek (pop (fx/sub context :history)))
        url (:url (fx/sub context response-by-request-id-sub new-active-request-id))]
    {:context (fx/swap-context context #(-> %
                                            (update :history pop)
                                            (assoc :typed-url url)))}))

;; end region

;; region views

(defn loading-view [_]
  {:fx/type :h-box
   :alignment :center
   :children [{:fx/type :progress-indicator}]})

(defn exception-view [{:keys [fx/context]}]
  (let [{:keys [url exception]} (fx/sub context current-response)]
    {:fx/type :v-box
     :alignment :center
     :children [{:fx/type :label
                 :text (str "Can't load url: " url)}
                {:fx/type :label
                 :text (or (ex-message exception) (str (class exception)))}]}))

(defn jsoup->clj [^Element jsoup-el]
  (let [attrs (into {}
                    (map (fn [[k v]]
                           [(keyword k) v]))
                    (.attributes jsoup-el))]
    {:tag (keyword (.tagName jsoup-el))
     :attrs attrs
     :children (mapv jsoup->clj (.children jsoup-el))}))

(defn- ->tree-item [x]
  {:fx/type :tree-item
   :expanded true
   :value x
   :children (map ->tree-item (:children x))})

(defn html-view [{:keys [tree]}]
  {:fx/type :tree-view
   :cell-factory (fn [{:keys [tag attrs]}]
                   {:text (str [tag attrs])})
   :root (->tree-item tree)})

(defn result-view [{:keys [fx/context]}]
  (let [{:keys [response]} (fx/sub context current-response)
        content-type (-> response :headers (get "Content-Type") (str/split #";\s*") first)]
    (case content-type
      "text/html"
      {:fx/type html-view
       :tree (jsoup->clj (Jsoup/parse (String. ^bytes (:body response))))}

      "text/plain"
      {:fx/type :scroll-pane
       :fit-to-width true
       :content {:fx/type :label
                 :wrap-text true
                 :text (String. ^bytes (:body response))}}

      ("image/png" "image/jpeg")
      {:fx/type :scroll-pane
       :fit-to-width true
       :fit-to-height true
       :content {:fx/type :v-box
                 :alignment :center
                 :children [{:fx/type :image-view
                             :image {:is (io/input-stream (:body response))}}]}}

      {:fx/type :scroll-pane
       :fit-to-width true
       :content {:fx/type :label
                 :wrap-text true
                 :text (str content-type ": " response)}})))

(defn current-page-view [{:keys [fx/context]}]
  (case (:result (fx/sub context current-response))
    :pending {:fx/type loading-view}
    :success {:fx/type result-view}
    :failure {:fx/type exception-view}
    nil {:fx/type :region}))

(defn toolbar-view [{:keys [fx/context]}]
  {:fx/type :h-box
   :spacing 10
   :children [{:fx/type :button
               :text "Back"
               :disabled (fx/sub context history-empty?-sub)
               :on-action {:event/type ::go-back}}
              {:fx/type :text-field
               :h-box/hgrow :always
               :text (fx/sub context :typed-url)
               :on-text-changed {:event/type ::type-url :fx/sync true}
               :on-key-pressed {:event/type ::key-press-url}}]})

(defn root-view [_]
  {:fx/type :stage
   :width 960
   :height 540
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :padding 10
                  :spacing 10
                  :children [{:fx/type toolbar-view}
                             {:fx/type current-page-view
                              :v-box/vgrow :always}]}}})

;; endregion

;; region bootstrap

(def app-event-handler
  (-> event-handler
      (fx/wrap-co-effects {:fx/context (fx/make-deref-co-effect *state)})
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
                  (fx/wrap-map-desc (fn [_] {:fx/type root-view})))
    :opts {:fx.opt/map-event-handler app-event-handler
           :fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(fx/mount-app *state app)

(app-event-handler {:event/type ::open-url :url "https://github.com/cljfx/cljfx/"})

;; endregion
