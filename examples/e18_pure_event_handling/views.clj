(ns e18-pure-event-handling.views
  (:require [cljfx.api :as fx]
            [e18-pure-event-handling.subs :as subs]
            [e18-pure-event-handling.events :as events]
            [clojure.java.io :as io])
  (:import [org.jsoup.nodes Element]
           [org.jsoup Jsoup]))

(defn- loading [_]
  {:fx/type :h-box
   :alignment :center
   :children [{:fx/type :progress-indicator}]})

(defn- exception [{:keys [fx/context]}]
  (let [{:keys [url exception]} (fx/sub-ctx context subs/current-response)]
    {:fx/type :v-box
     :alignment :center
     :children [{:fx/type :label
                 :text (str "Can't load url: " url)}
                {:fx/type :label
                 :text (or (ex-message exception) (str (class exception)))}]}))

(defn- jsoup->clj [^Element jsoup-el]
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

(defn- html [{:keys [tree]}]
  {:fx/type :tree-view
   :cell-factory {:fx/cell-type :tree-cell
                  :describe (fn [{:keys [tag attrs]}]
                              {:text (str [tag attrs])})}
   :root (->tree-item tree)})

(defn- result [{:keys [fx/context]}]
  (let [request-id (fx/sub-ctx context subs/current-request-id)
        content-type (fx/sub-ctx context subs/context-type request-id)
        ^bytes body (fx/sub-ctx context subs/body request-id)]
    (case content-type
      "text/html"
      {:fx/type html
       :tree (jsoup->clj (Jsoup/parse (String. body)))}

      "text/plain"
      {:fx/type :scroll-pane
       :fit-to-width true
       :content {:fx/type :label
                 :wrap-text true
                 :text (String. body)}}

      ("image/png" "image/jpeg")
      {:fx/type :scroll-pane
       :fit-to-width true
       :fit-to-height true
       :content {:fx/type :v-box
                 :alignment :center
                 :children [{:fx/type :image-view
                             :image {:is (io/input-stream body)}}]}}

      {:fx/type :scroll-pane
       :fit-to-width true
       :content {:fx/type :label
                 :wrap-text true
                 :text (str content-type ": " (String. body))}})))

(defn current-page [{:keys [fx/context]}]
  (case (:result (fx/sub-ctx context subs/current-response))
    :pending {:fx/type loading}
    :success {:fx/type result}
    :failure {:fx/type exception}
    nil {:fx/type :region}))

(defn toolbar [{:keys [fx/context]}]
  {:fx/type :h-box
   :spacing 10
   :children [{:fx/type :button
               :text "Back"
               :disable (fx/sub-ctx context subs/history-empty?)
               :on-action {:event/type ::events/go-back}}
              {:fx/type :text-field
               :h-box/hgrow :always
               :text (fx/sub-val context :typed-url)
               :on-text-changed {:event/type ::events/type-url :fx/sync true}
               :on-key-pressed {:event/type ::events/key-press-url}}]})

(defn root [_]
  {:fx/type :stage
   :width 960
   :height 540
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :padding 10
                  :spacing 10
                  :children [{:fx/type toolbar}
                             {:fx/type current-page
                              :v-box/vgrow :always}]}}})
