(ns cljfx.platform
  (:import [javafx.application Platform]))

(defmacro on-fx-thread [& body]
  `(let [*ret# (promise)]
     (if (Platform/isFxApplicationThread)
       (deliver *ret# (do ~@body))
       (do
         (Platform/runLater
           (fn []
             (let [result# (try
                             [nil (do ~@body)]
                             (catch Exception e#
                               [e# nil]))
                   [err# ~'_] result#]
               (deliver *ret# result#)
               (when err#
                 (.printStackTrace ^Throwable err#)))))
         (delay
           (let [[err# val#] @*ret#]
             (if err#
               (throw err#)
               val#)))))))
