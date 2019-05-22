(ns e10-multiple-windows
  (:require [cljfx.api :as fx])
  (:import [javafx.stage Screen]))

(defn window [{:keys [x y width height]}]
  {:fx/type :stage
   :always-on-top true
   :showing true
   :x x
   :y y
   :width width
   :height height
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :alignment :center
                  :children [{:fx/type :label
                              :text (str "Window at [" x ", " y "] "
                                         "with size " width "x" height)}]}}})

(def width 300)

(def height 100)

(def bottom
  (-> (Screen/getPrimary)
      .getBounds
      .getHeight
      (- height)))

(def right
  (-> (Screen/getPrimary)
      .getBounds
      .getWidth
      (- width)))

(fx/on-fx-thread
  (fx/create-component
    {:fx/type fx/ext-many
     :desc [{:fx/type window
             :x 0
             :y 0
             :width width
             :height height}
            {:fx/type window
             :x 0
             :y bottom
             :width width
             :height height}
            {:fx/type window
             :x right
             :y 0
             :width width
             :height height}
            {:fx/type window
             :x right
             :y bottom
             :width width
             :height height}]}))
