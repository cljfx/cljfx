(ns cljfx.jdk.platform
  (:import [javafx.application Platform]
           [java.util.logging LogManager]
           [java.io ByteArrayInputStream]))

(defn initialize []
  (try
    (when-not (Boolean/getBoolean "cljfx.log-javafx-warnings")
      (.readConfiguration
        (LogManager/getLogManager)
        (ByteArrayInputStream. (.getBytes "\"javafx\".level=SEVERE" "UTF-8"))))
    (Platform/startup #(Platform/setImplicitExit false))
    :cljfx.platform/initialized
    (catch IllegalStateException _
      :cljfx.platform/already-initialized)))