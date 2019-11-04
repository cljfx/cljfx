(ns cljfx.jdk.platform
  (:import [javafx.application Platform]))

(defn initialize []
  (try
    (Platform/startup #(Platform/setImplicitExit false))
    :cljfx.platform/initialized
    (catch IllegalStateException _
      :cljfx.platform/already-initialized)))