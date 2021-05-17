(ns e41-manage-instance
  (:require [cljfx.api :as fx])
  (:import [javafx.scene Scene]
           [javafx.stage Stage]
           [javafx.scene.layout VBox]
           [javafx.scene.control Label]))

(set! *warn-on-reflection* true)

(def externally-created-stage
  @(fx/on-fx-thread
     (doto (Stage.)
       (.setScene (Scene. (VBox.
                            ^"[Ljavafx.scene.Node;"
                            (into-array [(Label. "Unmanaged")]))))
       (.setWidth 400)
       (.setHeight 400)
       (.show))))

(Thread/sleep 1000)

@(fx/on-fx-thread
   (fx/create-component
     {:fx/type :stage
      :fx/manage-instance externally-created-stage
      :showing true
      :scene {:fx/type :scene
              :root {:fx/type :v-box
                     :children [{:fx/type :label
                                 :text "Managed"}]}}}))
