(ns e21-extension-lifecycles
  (:require [cljfx.api :as fx])
  (:import [javafx.scene Node]
           [javafx.animation TranslateTransition Interpolator]
           [javafx.util Duration]
           [javafx.scene.layout Region]))

(defn- animate-entrance [^Node node]
  (doto (TranslateTransition. (Duration/seconds 2) node)
    (.setFromY -100)
    (.setToY 0)
    (.setInterpolator Interpolator/EASE_OUT)
    (.play)))

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :always-on-top true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :alignment :center
                    :padding 100
                    :children [{:fx/type fx/ext-on-instance-lifecycle
                                :on-created animate-entrance
                                :desc {:fx/type :region
                                       :pref-width 20
                                       :pref-height 20
                                       :style {:-fx-background-color :red}}}
                               {:fx/type fx/ext-instance-factory
                                :create #(doto (Region.)
                                           (.setPrefWidth 20)
                                           (.setPrefHeight 20)
                                           (.setStyle "-fx-background-color: green;"))}]}}}))
