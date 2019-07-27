; Author: Ambrose Bonnaire-Sergeant
(ns e31-indefinite-transitions
  (:require [cljfx.api :as fx]))

;; A transition is a kind of animation in JavaFX which
;; usually operates on nodes. This file demonstrates
;; how to start transitions that run forever (by setting
;; :cycle-count to :indefinite) and attach them to arbitrary
;; nodes.

;; The logic to attach a transition to a node is in
;; [[start-transition-on]], and the actual definitions for
;; the animations in this file are [[indefinite-animations]].

(set! *warn-on-reflection* true)

(defn- let-refs [refs desc]
  {:fx/type fx/ext-let-refs
   :refs refs
   :desc desc})

(defn- get-ref [ref]
  {:fx/type fx/ext-get-ref
   :ref ref})

(def transition-target-prop
  "For transitions that target a node this map specifies
  the prop to write to. eg., :fade-transition takes :node prop,
  but :fill-transition takes a :shape prop."
  (into {}
        (concat (map vector
                     #{:fade-transition     
                       :scale-transition    
                       :rotate-transition   
                       :path-transition     
                       :translate-transition
                       :parallel-transition
                       :sequential-transition}
                     (repeat :node))
                (map vector
                     #{:fill-transition
                       :stroke-transition}
                     (repeat :shape)))))

(defn add-target-prop [desc node]
  (let [target-prop (get transition-target-prop (:fx/type desc))]
    (cond-> desc
      target-prop (assoc target-prop node))))

(defn start-transition-on
  "Returns the given desc the given transition animation attached
  and running."
  [{:keys [desc transition]}]
  (let-refs {::transition-node desc}
    (let [tn (get-ref ::transition-node)]
      (let-refs {::transition (-> transition
                                  (add-target-prop tn)
                                  (assoc :status :running))}
        tn))))

(defn on-grid [{:keys [rows columns children]}]
  {:pre [(vector? children)
         (<= (count children)
             (* rows columns))]}
  {:fx/type :grid-pane
   :row-constraints (repeat rows {:fx/type :row-constraints
                                  :percent-height (/ 100 rows)})
   :column-constraints (repeat columns {:fx/type :column-constraints
                                        :percent-width (/ 100 columns)})
   :children (let [loc (atom -1)]
               (vec
                 (for [row (range rows)
                       col (range columns)
                       :let [_ (swap! loc inc)]
                       :when (< @loc (count children))]
                   (-> (nth children @loc)
                       (assoc :grid-pane/row row
                              :grid-pane/column col)))))})

(defn with-header [text desc]
  {:fx/type :grid-pane
   :row-constraints (repeat 1 {:fx/type :row-constraints
                               :percent-height 100/1})
   :column-constraints (repeat 2 {:fx/type :column-constraints
                                  :percent-width 100/2})
   :children [{:fx/type :label
               :grid-pane/row 0
               :grid-pane/column 0
               :text text}
              (assoc desc
                     :grid-pane/row 0
                     :grid-pane/column 1)]})

; The animations
(def indefinite-animations
  [["Fade" {:fx/type :fade-transition
            :from-value 1.0
            :to-value 0.3
            :cycle-count :indefinite
            :duration [1 :s]
            :auto-reverse true}]
   ["Fill" {:fx/type :fill-transition
            :from-value :green
            :to-value :red
            :cycle-count :indefinite
            :duration [1 :s]
            :auto-reverse true}]
   ["Scale" {:fx/type :scale-transition
             :by-x 0.2
             :by-y 1.2
             :cycle-count :indefinite
             :duration [1 :s]
             :auto-reverse true}]
   ["Stroke" {:fx/type :stroke-transition
              :from-value :red
              :to-value :yellow
              :cycle-count :indefinite
              :duration [0.5 :s]
              :auto-reverse true}]
   ["Rotate" {:fx/type :rotate-transition
              :duration [0.5 :s]
              :from-angle 0
              :to-angle 185
              :cycle-count :indefinite
              :auto-reverse true
              :interpolator :ease-both}]
   ["Translate" {:fx/type :translate-transition
                 :duration [0.5 :s]
                 :from-x 0
                 :to-x 75
                 :interpolator :ease-both
                 :auto-reverse true
                 :cycle-count :indefinite}]
   ["Path" {:fx/type :path-transition
            :path {:fx/type :circle
                   :radius 35}
            :duration [1 :s]
            :interpolator :linear
            :orientation :orthogonal-to-tangent
            :cycle-count :indefinite}]
   ["Parallel" {:fx/type :parallel-transition
                :auto-reverse true
                :cycle-count :indefinite
                ; target node is automatically attached to children
                ; by JavaFX, if not already bound.
                :children [{:fx/type :rotate-transition
                            :duration [0.5 :s]
                            :from-angle 0
                            :to-angle 185
                            :interpolator :ease-both}
                           {:fx/type :translate-transition
                            :duration [0.5 :s]
                            :from-x 0
                            :to-x 75
                            :interpolator :ease-both}]}]
   ["Sequential" {:fx/type :sequential-transition
                  :auto-reverse true
                  :cycle-count :indefinite
                  :children [{:fx/type :rotate-transition
                              :duration [0.5 :s]
                              :from-angle 0
                              :to-angle 185
                              :interpolator :ease-both}
                             {:fx/type :translate-transition
                              :duration [0.5 :s]
                              :from-x 0
                              :to-x 75
                              :interpolator :ease-both}]}]
   ["Nested" (let [switch-props (fn [props p1 p2]
                                  (assoc props
                                         p2 (get props p1)
                                         p1 (get props p2)))
                   pause-1s {:fx/type :pause-transition
                             :duration [1 :s]}
                   rotate-right {:fx/type :rotate-transition
                                 :duration [0.5 :s]
                                 :from-angle 0
                                 :to-angle 185
                                 :interpolator :ease-both}
                   translate-right {:fx/type :translate-transition
                                    :duration [0.5 :s]
                                    :from-x 0
                                    :to-x 75
                                    :interpolator :ease-both}
                   rotate-left (switch-props rotate-right :from-angle :to-angle)
                   translate-left (switch-props translate-right :from-x :to-x)
                   parallel-dance-right {:fx/type :parallel-transition
                                         :children [rotate-right
                                                    translate-right]}
                   parallel-dance-left {:fx/type :parallel-transition
                                        :children [rotate-left
                                                   translate-left]}]
               {:fx/type :parallel-transition
                :cycle-count :indefinite
                :children [{:fx/type :fade-transition
                            :from-value 1.0
                            :to-value 0.3
                            :auto-reverse true
                            :duration [0.5 :s]
                            :cycle-count 6}
                           {:fx/type :sequential-transition
                            :children [pause-1s
                                       parallel-dance-right
                                       pause-1s
                                       parallel-dance-left]}]})]])

(defn view [{_ :state}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 600
   :height 500
   :scene {:fx/type :scene
           :root {:fx/type on-grid
                  :rows 5
                  :columns 2
                  :children (mapv
                              (fn [[header transition]]
                                (with-header
                                  header
                                  {:fx/type start-transition-on
                                   :transition transition
                                   ; run each animation on a small rectangle
                                   :desc {:fx/type :rectangle
                                          :width 20
                                          :height 50}}))
                              indefinite-animations)}}})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type view}))

; how to add state
(comment
  (defn init-state []
    {})

  (declare *state renderer)

  (when (and (.hasRoot #'*state)
             (.hasRoot #'renderer))
    (fx/unmount-renderer *state renderer)
    (reset! *state (init-state)))

  (def *state
    (atom (init-state)))

  (def renderer
    (fx/create-renderer
      :middleware (fx/wrap-map-desc (fn [state]
                                      {:fx/type view
                                       :state state}))))

  (fx/mount-renderer *state renderer))
