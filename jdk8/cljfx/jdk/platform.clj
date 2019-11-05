(ns cljfx.jdk.platform
  (:import [javafx.embed.swing JFXPanel]
           [javafx.application Platform]))

(defn initialize []
  (JFXPanel.)
  (Platform/setImplicitExit false)
  :cljfx.platform/initialized)
