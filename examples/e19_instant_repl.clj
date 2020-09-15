(ns e19-instant-repl
  (:require [cljfx.api :as fx]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io])
  (:import (java.io BufferedReader)
           (clojure.lang LineNumberingPushbackReader)))

(def *text
  (atom "{:a 1}"))

(defmulti handle :event/type)

(defmethod handle ::text-changed [{:keys [fx/event]}]
  [[:text event]])

(defn root-view [{:keys [text]}]
  {:fx/type :stage
   :width 960
   :height 400
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

                              :content
                              {:fx/type :label
                               :style {:-fx-font-family "monospace"}
                               :wrap-text true
                               :text
                               (with-out-str
                                 (let [EOF (Object.)
                                       rdr
                                       (-> text char-array (io/reader)
                                           BufferedReader.
                                           LineNumberingPushbackReader.)]
                                   (try (loop []
                                          (let [form (read {:eof EOF} rdr)]
                                            (when-not (identical? form EOF)
                                              (pprint/pprint form)
                                              (print ";=> ")
                                              (pprint/pprint (eval form))
                                              (recur))))
                                        (catch Throwable ex
                                          (println
                                            (-> ex
                                                Throwable->map
                                                clojure.main/ex-triage
                                                clojure.main/ex-str))))))}}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [text]
                                    {:fx/type root-view
                                     :text text}))
    :opts {:fx.opt/map-event-handler (fx/wrap-effects handle
                                       {:text (fx/make-reset-effect *text)})}))

(fx/mount-renderer *text renderer)
