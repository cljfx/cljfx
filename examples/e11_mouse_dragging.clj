(ns e11-mouse-dragging
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.input MouseEvent]))

(def *state
  (atom [{:x 100 :y 100}
         {:x 200 :y 100}]))

(defmulti event-handler :event/type)

(defmethod event-handler ::mouse-dragged [e]
  (let [index (:index e)
        fx-event ^MouseEvent (:fx/event e)]
    (swap! *state update index assoc :x (.getSceneX fx-event) :y (.getSceneY fx-event))))

(defmethod event-handler :default [e]
  (prn e))

(defn point-view [{:keys [point index]}]
  {:fx/type :circle
   :layout-x (:x point)
   :layout-y (:y point)
   :on-mouse-dragged {:event/type ::mouse-dragged :index index}
   :radius 10})

(defn root [{:keys [points]}]
  {:fx/type :stage
   :showing true
   :width 300
   :height 200
   :scene {:fx/type :scene
           :root {:fx/type :group
                  :children (map-indexed
                              (fn [index point]
                                {:fx/type point-view
                                 :index index
                                 :point point})
                              points)}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [points]
                                    {:fx/type root
                                     :points points}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
