
;; This file is supposed to be explored from the REPL, evaluating forms one
;; by one from top to bottom.

(ns e12-interactive-development
  (:require [cljfx.api :as fx]))

;; I want to build an interactive chart that shows how bouncing object falls
;; on the ground. I want to be able to edit gravity and friction to see how
;; it affects object's behavior, so I will put it into state:

(def *state
  (atom {:gravity 10
         :friction 0.4}))

;; I want to have map event handlers extensible during runtime to avoid full app
;; restarts. One way is using vars instead of functions to get that kind of
;; behavior, but I'll go with another way: multi-methods.

(defmulti event-handler :event/type)

;; Now we'll create our app with dummy root view

(defn root-view [{{:keys [gravity friction]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :h-box
                  :children [{:fx/type :label
                              :text (str "g = " gravity ", f = " friction)}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type root-view
                                     :state state}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)

;; At this point, really tiny window appears that displays current gravity and
;; friction. We want to have an ability to change these values, so let's create
;; some slider views for them:

(defn slider-view [{:keys [min max value]}]
  {:fx/type :slider
   :min min
   :max max
   :value value})

;; Now we will update our root view to display these sliders:

(defn root-view [{{:keys [gravity friction]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :h-box
                  :children [{:fx/type slider-view
                              :min 0
                              :max 100
                              :value gravity}
                             {:fx/type slider-view
                              :min 0
                              :max 1
                              :value friction}]}}})

;; Now we updated our root function, but window didn't change. It happens
;; because cljfx has no way to know if definition of some component functions is
;; changed. But we can ask renderer to refresh itself by calling it without any
;; arguments:

(renderer)

;; Now small label got replaced with 2 sliders. Problem is, there are no labels
;; on them, so users can't really see what these sliders mean, so let's fix it:

(defn slider-view [{:keys [min max value label]}]
  {:fx/type :v-box
   :children [{:fx/type :label
               :text label}
              {:fx/type :slider
               :min min
               :max max
               :value value
               :major-tick-unit max
               :show-tick-labels true}]})

(defn root-view [{{:keys [gravity friction]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :h-box
                  :spacing 10
                  :children [{:fx/type slider-view
                              :min 0
                              :max 100
                              :value gravity
                              :label "Gravity"}
                             {:fx/type slider-view
                              :min 0
                              :max 1
                              :label "Friction"
                              :value friction}]}}})

(renderer)

;; Great, time to add a chart that uses gravity and friction, but first let's
;; try to display something dummy to make sure it works

(defn chart-view [{:keys [gravity friction]}]
  {:fx/type :line-chart
   :x-axis {:fx/type :number-axis
            :label "Time"}
   :y-axis {:fx/type :number-axis
            :label "Y"}
   :data [{:fx/type :xy-chart-series
           :name "Position by time"
           :data (for [t (range 100)]
                   {:fx/type :xy-chart-data
                    :x-value t
                    :y-value t})}]})

(defn root-view [{{:keys [gravity friction]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :spacing 20
                  :children [{:fx/type chart-view
                              :gravity gravity
                              :friction friction}
                             {:fx/type :h-box
                              :spacing 10
                              :alignment :center
                              :children [{:fx/type slider-view
                                          :min 0
                                          :max 100
                                          :value gravity
                                          :label "Gravity"}
                                         {:fx/type slider-view
                                          :min 0
                                          :max 1
                                          :label "Friction"
                                          :value friction}]}]}}})

(renderer)

;; Now chart is added to a window. Everything looks fine, time to do some
;; simulation:

(defn simulate-step [{:keys [velocity y]} gravity friction]
  (let [new-velocity (* (- velocity gravity) (- 1 friction))
        new-y (+ y new-velocity)]
    (if (neg? new-y)
      {:velocity (- new-velocity) :y 0}
      {:velocity new-velocity :y new-y})))

(defn chart-view [{:keys [gravity friction]}]
  {:fx/type :line-chart
   :x-axis {:fx/type :number-axis
            :label "Time"}
   :y-axis {:fx/type :number-axis
            :label "Y"}
   :data [{:fx/type :xy-chart-series
           :name "Position by time"
           :data (->> {:velocity 0 :y 100}
                      (iterate #(simulate-step % gravity friction))
                      (take 100)
                      (map-indexed (fn [index {:keys [y]}]
                                     {:fx/type :xy-chart-data
                                      :x-value index
                                      :y-value y})))}]})

(renderer)

;; Okay, there are some results showing, but there is no bouncing, probably
;; gravity and friction have some weird values. It's time to make it all alive!
;; What remains is propagating changes in slider values to our model, so let's
;; add it

(defmethod event-handler ::set-friction [e]
  (swap! *state assoc :friction (:fx/event e)))

(defmethod event-handler ::set-gravity [e]
  (swap! *state assoc :gravity (:fx/event e)))

(defn slider-view [{:keys [min max value label event]}]     ;; add event as arg
  {:fx/type :v-box
   :children [{:fx/type :label
               :text label}
              {:fx/type :slider
               :min min
               :max max
               :value value
               :on-value-changed {:event/type event}        ;; fire it on value
               :major-tick-unit max
               :show-tick-labels true}]})

(defn root-view [{{:keys [gravity friction]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :spacing 20
                  :children [{:fx/type chart-view
                              :gravity gravity
                              :friction friction}
                             {:fx/type :h-box
                              :spacing 10
                              :alignment :center
                              :children [{:fx/type slider-view
                                          :min 0
                                          :max 100
                                          :value gravity
                                          :label "Gravity"
                                          :event ::set-gravity} ;; provide events
                                         {:fx/type slider-view
                                          :min 0
                                          :max 1
                                          :label "Friction"
                                          :value friction
                                          :event ::set-friction}]}]}}})

(renderer)

;; Nice, now playing with sliders makes chart data change! And all this is
;; done in runtime! Looks like the problem was that gravity is too high, so as a
;; final touch let's make it lower (both current value and slider range)

(swap! *state assoc :gravity 1)

(defn root-view [{{:keys [gravity friction]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :spacing 20
                  :children [{:fx/type chart-view
                              :gravity gravity
                              :friction friction}
                             {:fx/type :h-box
                              :spacing 10
                              :alignment :center
                              :children [{:fx/type slider-view
                                          :min 0
                                          :max 5  ;; 100 -> 5
                                          :value gravity
                                          :label "Gravity"
                                          :event ::set-gravity}
                                         {:fx/type slider-view
                                          :min 0
                                          :max 1
                                          :label "Friction"
                                          :value friction
                                          :event ::set-friction}]}]}}})

(renderer)
