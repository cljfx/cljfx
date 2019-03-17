(ns e23-accelerators
  (:require [cljfx.api :as fx]))

(def *state
  (atom true))

(defmulti event-handler :event/type)

(defmethod event-handler ::close [_]
  (reset! *state false))

(defn root-view [{:keys [showing]}]
  {:fx/type :stage
   :showing showing
   :scene {:fx/type :scene
           :accelerators {[:escape] {:event/type ::close}}
           :root {:fx/type :v-box
                  :padding 20
                  :children [{:fx/type :label
                              :text "Press Esc to exit"}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc
                  (fn [showing]
                    {:fx/type root-view
                     :showing showing}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
