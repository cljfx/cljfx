(ns cljfx.platform
  (:import [javafx.application Platform]))

(defmacro run-later [& body]
  `(let [*result# (promise)]
     (Platform/runLater
       (fn []
         (let [result# (try
                         [nil (do ~@body)]
                         (catch Exception e#
                           [e# nil]))
               [err# ~'_] result#]
           (deliver *result# result#)
           (when err#
             (.printStackTrace ^Throwable err#)))))
     (delay
       (let [[err# val#] @*result#]
         (if err#
           (throw err#)
           val#)))))

(defmacro on-fx-thread [& body]
  `(if (Platform/isFxApplicationThread)
     (deliver (promise) (do ~@body))
     (run-later ~@body)))
