(ns e26-tooltips
  (:require [cljfx.api :as fx]
            [cljfx.ext.node :as fx.ext.node])
  (:import [javafx.scene.input ScrollEvent]))

;; Hover over circle, observe tooltip indicating radius, scroll to change it
;; Note that this tooltip configuration only needed for non-control tooltips, because
;; controls have `:tooltip` prop which makes it easier to specify tooltips for them

(def *radius
  (atom 20))

(defn- circle-view [{:keys [radius]}]
  {:fx/type fx.ext.node/with-tooltip-props
   :props {:tooltip {:fx/type :tooltip
                     ;; jdk 11 only
                     ;:show-duration [1 :h]
                     :text (str "My radius is " radius)}}
   :desc {:fx/type :circle
          :radius radius
          :on-scroll {:event/type ::scroll}}})

(def renderer
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [radius]
        {:fx/type :stage
         :showing true
         :width 250
         :height 250
         :scene {:fx/type :scene
                 :root {:fx/type :v-box
                        :alignment :center
                        :children [{:fx/type circle-view
                                    :radius radius}]}}}))
    :opts
    {:fx.opt/map-event-handler (fn [{:keys [event/type ^ScrollEvent fx/event]}]
                                 (case type
                                   ::scroll
                                   (swap! *radius (fn [radius]
                                                    (-> radius
                                                        (+ (/ (.getDeltaY event) 10))
                                                        (min 100)
                                                        (max 10))))))}))

(fx/mount-renderer *radius renderer)
