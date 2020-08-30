(ns cljfx.fx
  "Part of a public API"
  (:require [clojure.java.io :as io]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.string :as str])
  (:import (java.io File)))

(defn lazy-load [sym-expr]
  (delay @(requiring-resolve sym-expr)))

(def keyword->lifecycle-delay
  (let [sep (System/getProperty "file.separator")]
    (->> (io/file (str "src" sep "cljfx" sep "fx"))
         (#(ns-find/find-sources-in-dir % ns-find/clj))
         (map #(read-string (str "(" (slurp (.getAbsolutePath ^File %)) ")")))
         (filter (fn [top-forms]
                   (seq (filter (fn [forms]
                                  (and (list? forms)
                                       (= (take 2 forms) '(def lifecycle))))
                                top-forms))))
         (map (fn [form]
                (let [ns-str        (-> form first second name)
                      fx-kw         (-> ns-str (str/split #"\.") last keyword)
                      lifecycle-sym (symbol (str ns-str "/lifecycle"))]
                  [fx-kw (lazy-load lifecycle-sym)])))
         (into {}))))

(defn get-available-fx-types
  "Returns a list of FX types (JavaFX components)"
  []
  (sort (keys keyword->lifecycle-delay)))

(defn keyword->lifecycle [kw]
  (when-let [*delay (keyword->lifecycle-delay kw)]
    @*delay))
