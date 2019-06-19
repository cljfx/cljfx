(ns e28-canvas
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.canvas Canvas]
           [javafx.scene.paint Color]))

(def *progress
  (atom 0.3))

(defn canvas-progress-bar [{:keys [progress width height]}]
  {:fx/type :canvas
   :width width
   :height height
   :draw (fn [^Canvas canvas]
           (doto (.getGraphicsContext2D canvas)
             (.clearRect 0 0 width height)
             (.setFill Color/LIGHTGREY)
             (.fillRoundRect 0 0 width height height height)
             (.setFill Color/GREEN)
             (.fillRoundRect 0 0 (* width progress) height height height)))})

(def renderer
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [progress]
        {:fx/type :stage
         :showing true
         :scene {:fx/type :scene
                 :root {:fx/type :v-box
                        :padding 100
                        :spacing 50
                        :children [{:fx/type canvas-progress-bar
                                    :width 100
                                    :height 10
                                    :progress progress}
                                   {:fx/type :slider
                                    :pref-width 100
                                    :min 0
                                    :max 1
                                    :value progress
                                    :on-value-changed #(reset! *progress %)}]}}}))))

(fx/mount-renderer *progress renderer)
