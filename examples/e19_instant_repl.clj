(ns e19-instant-repl
  (:require [cljfx.api :as fx]
            [clojure.pprint :as pprint]))

(def *text
  (atom "{:a 1}"))

(defmulti handle :event/type)

(defmethod handle ::text-changed [{:keys [fx/event]}]
  [[:text event]])

(defn root-view [{:keys [text]}]
  {:fx/type :stage
   :width 960
   :height 960
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :grid-pane
                  :padding 10
                  :hgap 10
                  :column-constraints [{:fx/type :column-constraints
                                        :percent-width 100/2}
                                       {:fx/type :column-constraints
                                        :percent-width 100/2}]
                  :row-constraints [{:fx/type :row-constraints
                                     :percent-height 100}]
                  :children [{:fx/type :text-area
                              :style {:-fx-font-family "monospace"}
                              :text text
                              :on-text-changed {:event/type ::text-changed}}
                             {:fx/type :scroll-pane
                              :grid-pane/column 1
                              :content {:fx/type :label
                                        :style {:-fx-font-family "monospace"}
                                        :wrap-text true
                                        :text (with-out-str
                                                (try
                                                  (let [form (read-string text)]
                                                    (pprint/pprint form)
                                                    (print "=> ")
                                                    (pprint/pprint (eval form)))
                                                  (catch Exception e
                                                    (pprint/pprint e))))}}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [text]
                                    {:fx/type root-view
                                     :text text}))
    :opts {:fx.opt/map-event-handler (-> handle
                                         (fx/wrap-effects {:text (fx/make-reset-effect *text)})
                                         (fx/wrap-async))}))

(fx/mount-renderer *text renderer)
