(ns splash
  "An example cljfx UI that uses a splash screen.
  Hooked up via splash.PreloadingApplication/CLJFX_MAIN."
  (:require [cljfx.api :as fx]))

(defn -main
  [& args]
  (fx/on-fx-thread
    (fx/create-component
      {:fx/type :stage
       :showing true
       :title "Cljfx example"
       :width 300
       :height 100
       :scene {:fx/type :scene
               :root {:fx/type :v-box
                      :alignment :center
                      :children [{:fx/type :label
                                  :text "Hello world"}]}}})))
