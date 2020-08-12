(ns e20-markdown-editor
  (:require [cljfx.api :as fx]
            [clojure.string :as str]
            [clojure.core.cache :as cache])
  (:import [de.codecentric.centerdevice.javafxsvg SvgImageLoaderFactory]
           [de.codecentric.centerdevice.javafxsvg.dimension PrimitiveDimensionProvider]
           [java.awt Desktop]
           [java.io File]
           [java.net URI]
           [org.commonmark.node Node]
           [org.commonmark.parser Parser]))

(SvgImageLoaderFactory/install (PrimitiveDimensionProvider.))

(def *context
  (atom
    (fx/create-context {:typed-text (slurp "README.md")}
                       #(cache/lru-cache-factory % :threshold 4096))))

(defn commonmark->clj [^Node node]
  (let [tag (->> node
                 .getClass
                 .getSimpleName
                 (re-seq #"[A-Z][a-z]+")
                 (map str/lower-case)
                 (str/join "-")
                 keyword)
        all-attrs (->> node
                       bean
                       (map (fn [[k v]]
                              [(->> k
                                    name
                                    (re-seq #"[A-Z]?[a-z]+")
                                    (map str/lower-case)
                                    (str/join "-")
                                    keyword)
                               v]))
                       (into {}))]
    {:tag tag
     :attrs (dissoc all-attrs :next :previous :class :first-child :last-child :parent)
     :children (->> node
                    .getFirstChild
                    (iterate #(.getNext ^Node %))
                    (take-while some?)
                    (mapv commonmark->clj))}))

(defn node-sub [context]
  (-> (Parser/builder)
      .build
      (.parse (fx/sub-val context :typed-text))
      commonmark->clj))

(defmulti handle-event :event/type)

(defmethod handle-event :default [e]
  (prn e))

(defmethod handle-event ::type-text [{:keys [fx/event fx/context]}]
  {:context (fx/swap-context context assoc :typed-text event)})

(defmulti md->fx :tag)

(defn md-view [{:keys [node]}]
  (md->fx node))

(defmethod md->fx :heading [{children :children {:keys [level]} :attrs}]
  {:fx/type :text-flow
   :style-class ["heading" (str "level-" level)]
   :children (for [node children]
               {:fx/type md-view
                :node node})})

(defmethod md->fx :paragraph [{children :children}]
  {:fx/type :text-flow
   :style-class "paragraph"
   :children (for [node children]
               {:fx/type md-view
                :node node})})

(defmethod md->fx :text [{{:keys [literal]} :attrs}]
  {:fx/type :text
   :cache true
   :cache-hint :speed
   :text literal})

(defmethod md->fx :code [{{:keys [literal]} :attrs}]
  {:fx/type :label
   :cache true
   :cache-hint :speed
   :style-class "code"
   :text literal})

(defmethod md->fx :fenced-code-block [{{:keys [literal]} :attrs}]
  {:fx/type :v-box
   :padding {:top 9}
   :children [{:fx/type :scroll-pane
               :style-class ["scroll-pane" "code-block"]
               :fit-to-width true
               :content {:fx/type :label
                         :cache true
                         :cache-hint :speed
                         :max-width ##Inf
                         :min-width :use-pref-size
                         :text literal}}]})

(defmethod md->fx :indented-code-block [{{:keys [literal]} :attrs}]
  {:fx/type :v-box
   :padding {:top 9}
   :children [{:fx/type :scroll-pane
               :style-class ["scroll-pane" "code-block"]
               :fit-to-width true
               :content {:fx/type :label
                         :cache true
                         :cache-hint :speed
                         :max-width ##Inf
                         :min-width :use-pref-size
                         :text literal}}]})

(defmethod md->fx :link [{{:keys [^String destination]} :attrs children :children}]
  (let [link {:fx/type :hyperlink
              :on-action (fn [_]
                           (future
                             (try
                               (if (str/starts-with? destination "http")
                                 (.browse (Desktop/getDesktop) (URI. destination))
                                 (.open (Desktop/getDesktop) (File. destination)))
                               (catch Exception e
                                 (.printStackTrace e)))))}]
    (if (and (= 1 (count children))
             (= :text (:tag (first children))))
      (assoc link :text (-> children first :attrs :literal))
      (assoc link :graphic {:fx/type :h-box
                            :children (for [node children]
                                        {:fx/type md-view
                                         :node node})}))))

(defmethod md->fx :strong-emphasis [{:keys [children]}]
  (if (and (= 1 (count children))
           (= :text (:tag (first children))))
    {:fx/type :text
     :cache true
     :cache-hint :speed
     :style-class "strong-emphasis"
     :text (-> children first :attrs :literal)}
    {:fx/type :h-box
     :cache true
     :style-class "strong-emphasis"
     :children (for [node children]
                 {:fx/type md-view
                  :node node})}))

(defmethod md->fx :emphasis [{:keys [children]}]
  (if (and (= 1 (count children))
           (= :text (:tag (first children))))
    {:fx/type :text
     :cache true
     :cache-hint :speed
     :style-class "emphasis"
     :text (-> children first :attrs :literal)}
    {:fx/type :h-box
     :style-class "emphasis"
     :children (for [node children]
                 {:fx/type md-view
                  :node node})}))

(defmethod md->fx :soft-line-break [_]
  {:fx/type :text
   :text " "})

(defmethod md->fx :document [{:keys [children]}]
  {:fx/type :v-box
   :style-class "document"
   :children (for [node children]
               {:fx/type md-view
                :node node})})

(defmethod md->fx :image [{{:keys [destination]} :attrs}]
  {:fx/type :image-view
   :image {:url (if (str/starts-with? destination "http")
                  destination
                  (str "file:" destination))
           :background-loading true}})

(defmethod md->fx :bullet-list [{{:keys [bullet-marker]} :attrs children :children}]
  {:fx/type :v-box
   :style-class "md-list"
   :children (for [node children]
               {:fx/type :h-box
                :alignment :baseline-left
                :spacing 4
                :children [{:fx/type :label
                            :min-width :use-pref-size
                            :cache true
                            :cache-hint :speed
                            :text (str bullet-marker)}
                           {:fx/type md-view
                            :node node}]})})

(defmethod md->fx :ordered-list [{{:keys [delimiter start-number]} :attrs
                                  children :children}]
  {:fx/type :v-box
   :style-class "md-list"
   :children (map (fn [child number]
                    {:fx/type :h-box
                     :alignment :baseline-left
                     :spacing 4
                     :children [{:fx/type :label
                                 :cache true
                                 :cache-hint :speed
                                 :min-width :use-pref-size
                                 :text (str number delimiter)}
                                (assoc (md->fx child)
                                  :h-box/hgrow :always)]})
                  children
                  (range start-number ##Inf))})

(defmethod md->fx :list-item [{:keys [children]}]
  {:fx/type :v-box
   :children (for [node children]
               {:fx/type md-view
                :node node})})

(defmethod md->fx :default [{:keys [tag attrs children]}]
  {:fx/type :v-box
   :children [{:fx/type :label
               :cache true
               :cache-hint :speed
               :style {:-fx-background-color :red}
               :text (str tag " " attrs)}
              {:fx/type :v-box
               :padding {:left 10}
               :children (for [node children]
                           {:fx/type md-view
                            :node node})}]})

(defn note-input [{:keys [fx/context]}]
  {:fx/type :text-area
   :style-class "input"
   :text (fx/sub-val context :typed-text)
   :on-text-changed {:event/type ::type-text :fx/sync true}})

(defn note-preview [{:keys [fx/context]}]
  {:fx/type :scroll-pane
   :fit-to-width true
   :content {:fx/type md-view
             :node (fx/sub-ctx context node-sub)}})

(def app
  (fx/create-app *context
    :event-handler handle-event
    :desc-fn (fn [_]
               {:fx/type :stage
                :showing true
                :scene {:fx/type :scene
                        :stylesheets #{"markdown.css"}
                        :root {:fx/type :grid-pane
                               :padding 10
                               :hgap 10
                               :column-constraints [{:fx/type :column-constraints
                                                     :percent-width 100/2}
                                                    {:fx/type :column-constraints
                                                     :percent-width 100/2}]
                               :row-constraints [{:fx/type :row-constraints
                                                  :percent-height 100}]
                               :children [{:fx/type note-input
                                           :grid-pane/column 0}
                                          {:fx/type note-preview
                                           :grid-pane/column 1}]}}})))
