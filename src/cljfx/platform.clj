(ns cljfx.platform
  "Part of a public API"
  (:require [cljfx.jdk.platform :as jdk.platform])
  (:import [javafx.application Platform]))

(defmacro run-later [& body]
  `(let [*result# (promise)]
     (Platform/runLater
       (bound-fn []
         (let [result# (try
                         [nil (do ~@body)]
                         (catch Throwable e#
                           [e# nil]))
               [err# ~'_] result#]
           (deliver *result# result#))))
     (delay
       (let [[err# val#] @*result#]
         (if err#
           (throw err#)
           val#)))))

(defmacro on-fx-thread [& body]
  `(if (Platform/isFxApplicationThread)
     (try
       (deliver (promise) (do ~@body))
       (catch Throwable e#
         (delay (throw e#))))
     (run-later ~@body)))

(defn initialize []
  (jdk.platform/initialize))
