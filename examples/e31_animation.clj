(ns e31-animation
  (:require [cljfx.api :as fx]
            [cljfx.prop :as prop]
            [cljfx.composite :as composite]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            )
  (:import [java.util Collection]
           [javafx.event EventHandler]
           [javafx.scene Node]
           [javafx.scene.transform Transform]
           [javafx.animation TranslateTransition Interpolator Transition Timeline]
           [javafx.util Duration]
           [javafx.scene.layout Region]
           [javafx.animation AnimationTimer Animation ParallelTransition RotateTransition
            PathTransition$OrientationType
            FadeTransition StrokeTransition FillTransition PauseTransition
            ScaleTransition
            KeyFrame KeyValue SequentialTransition PathTransition]))

(set! *warn-on-reflection* true)

;;;;;;;;;;;
;; Setup ;;
;;;;;;;;;;;

;; Interfaces

(defmacro compile-if [cond & args]
  (when (eval cond)
    `(do ~@args)))

(compile-if (not (resolve 'ISetTimerHandler))
  (definterface ISetTimerHandler
    (setTimerHandler [handler])))

;; Coercions

(defn coerce-animation [x]
  (cond
    (= :indefinite x) Animation/INDEFINITE
    :else (int x)))

(defn coerce-orientation [x]
  (cond
    (instance? PathTransition$OrientationType x) x
    :else (case x
            :none PathTransition$OrientationType/NONE
            :orthogonal-to-tanget PathTransition$OrientationType/ORTHOGONAL_TO_TANGENT
            (coerce/fail PathTransition$OrientationType x))))

(defn coerce-interpolator [x]
  (cond
    (instance? Interpolator x) x
    (vector? x) (condp = (nth x 0)
                  :spline (case (count x)
                            5 (let [[_ x1 y1 x2 y2] x]
                                (Interpolator/SPLINE (double x1)
                                                     (double y1)
                                                     (double x2)
                                                     (double y2)))
                             (coerce/fail Interpolator x))
                  :tangent (case (count x)
                             3 (let [[_ t v] x]
                                 (Interpolator/TANGENT (coerce/duration t)
                                                       (double v)))
                             5 (let [[_ t1 v1 t2 v2] x]
                                 (Interpolator/TANGENT (coerce/duration t1)
                                                       (double v1)
                                                       (coerce/duration t2)
                                                       (double v2)))
                             (coerce/fail Interpolator x))
                  (coerce/fail Interpolator x))
    :else (case x
            :discrete Interpolator/DISCRETE
            :ease-both Interpolator/EASE_BOTH
            :ease-in Interpolator/EASE_IN
            :ease-out Interpolator/EASE_OUT
            :linear Interpolator/LINEAR
            (coerce/fail Interpolator x))))

(defn coerce-key-value [x]
  (cond
    (instance? KeyValue x) x
    (vector? x) (case (count x)
                  2 (let [[target end-value] x]
                      (KeyValue. target end-value))
                  3 (let [[target end-value interpolator] x]
                      (KeyValue. target
                                 end-value
                                 (coerce-interpolator interpolator)))
                  (coerce/fail KeyValue x))
    (map? x) (let [target (:target x)
                   end-value (:end-value x)
                   interpolator (coerce-interpolator (:interpolator x :linear))]
               (KeyValue. target end-value interpolator))
    :else (coerce/fail KeyValue x)))

(defn coerce-key-frame [x]
  (cond
    (instance? KeyFrame x) x
    (map? x) (let [^Duration d (coerce/duration (:time x))
                   ^String s (some-> (:name x) str)
                   ^EventHandler e (some-> (:on-finished x) coerce/event-handler)
                   ^Collection c (some->> (:values x) (mapv coerce-key-value))]
               (KeyFrame. d s e c))
    :else (coerce/fail KeyFrame x)))

;; Animation

(def animation-props
  (composite/props Animation
    :auto-reverse [:setter lifecycle/scalar :default false]
    :cycle-count [:setter lifecycle/scalar :coerce coerce-animation :default 1.0]
    :on-cycle-count-changed [:property-change-listener lifecycle/change-listener]
    :delay [:setter lifecycle/scalar :default (coerce/duration 0)]
    :on-finished [:setter lifecycle/event-handler :coerce coerce/event-handler :default nil]
    :rate [:setter lifecycle/scalar :coerce double :default 1.0]
    :jump-to [(mutator/setter
                #(if (string? %2)
                   (.jumpTo ^Animation %1 ^String %2)
                   (.jumpTo ^Animation %1 ^Duration %2)))
              lifecycle/scalar
              :coerce (fn [x]
                        (if (string? x)
                          x
                          (coerce/duration x)))]
    :on-status-changed [:property-change-listener lifecycle/change-listener]
    :status [(mutator/setter
               #(case %2
                  :running (.play ^Animation %1)
                  :paused (.pause ^Animation %1)
                  :stopped (.stop ^Animation %1)))
             lifecycle/scalar
             :default :stopped]))

;; AnimationTimer

(def animation-timer-lifecycle
  (-> (composite/lifecycle
        {:ctor (fn [handler]
                 (let [vhandler (volatile! handler)]
                   (proxy [AnimationTimer] []
                     (handle [now]
                       (when-some [handler @vhandler]
                         (handler now)))
                     (setTimerHandler [handler]
                       (vreset! vhandler handler)))))
         :args [:handler]
         :props {:handler (prop/make (mutator/setter
                                       #(.setTimerHandler ^ISetTimerHandler %1 %2))
                                     lifecycle/event-handler)
                 ;TODO is :state a good name?
                 :state (prop/make (mutator/setter
                                     #(case %2
                                        :starting (.start ^AnimationTimer %1)
                                        :stopped (.stop ^AnimationTimer %1)))
                                   lifecycle/scalar
                                   :default :stopped)}})
      (lifecycle/wrap-on-delete
        #(.stop ^AnimationTimer %1))))


;; Transition

(def transition-props
  (merge
    animation-props
    (composite/props
      Transition
      :interpolator [:setter lifecycle/scalar
                     :coerce coerce-interpolator
                     :default :ease-in])))

;; TranslateTransition

(def translate-transition-props
  (merge
    transition-props
    (composite/props
      TranslateTransition
      :by-x [:setter lifecycle/scalar :coerce double :default 0.0]
      :by-y [:setter lifecycle/scalar :coerce double :default 0.0]
      :by-z [:setter lifecycle/scalar :coerce double :default 0.0]
      :duration [:setter lifecycle/scalar :coerce coerce/duration
                 :default 400]
      :from-x [:setter lifecycle/scalar :coerce double :default ##NaN]
      :from-y [:setter lifecycle/scalar :coerce double :default ##NaN]
      :from-z [:setter lifecycle/scalar :coerce double :default ##NaN]
      :node [:setter lifecycle/dynamic]
      :to-x [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-y [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-z [:setter lifecycle/scalar :coerce double :default ##NaN]
      :state [(mutator/setter
                #(case %2
                   :playing (.play ^TranslateTransition %1)
                   :stopped (.stop ^TranslateTransition %1)))
              lifecycle/scalar
              :default :stopped])))

(def translate-transition-lifecycle
  (composite/describe TranslateTransition
    :ctor []
    :prop-order {:status 1}
    :props translate-transition-props))

;; RotateTransition

(def rotate-transition-props
  (merge transition-props
         (composite/props
           RotateTransition
           :axis [:setter lifecycle/scalar :coerce coerce/point-3d]
           :by-angle [:setter lifecycle/scalar :coerce double :default 0.0]
           :duration [:setter lifecycle/scalar :coerce coerce/duration :default 0]
           :from-angle [:setter lifecycle/scalar :coerce double :default ##NaN]
           :to-angle [:setter lifecycle/scalar :coerce double :default ##NaN]
           :node [:setter lifecycle/dynamic])))

(def rotate-transition-lifecycle
  (composite/describe RotateTransition
    :ctor []
    :prop-order {:status 1}
    :props rotate-transition-props))

;; SequentialTransition

(def sequential-transition-props
  (merge transition-props
         (composite/props
           SequentialTransition
           :children [:list lifecycle/dynamics]
           :node [:setter lifecycle/dynamic])))

(def sequential-transition-lifecycle
  (composite/describe SequentialTransition
    :ctor []
    :prop-order {:status 1}
    :props sequential-transition-props))

;; ParallelTransition

(def parallel-transition-props
  (merge transition-props
         (composite/props
           ParallelTransition
           :children [:list lifecycle/dynamics]
           :node [:setter lifecycle/dynamic])))

(def parallel-transition-lifecycle
  (composite/describe ParallelTransition
    :ctor []
    :prop-order {:status 1}
    :props parallel-transition-props))

;; PathTransition

(def path-transition-props
  (merge transition-props
         (composite/props
           PathTransition
           :node [:setter lifecycle/dynamic]
           :duration [:setter lifecycle/scalar :coerce coerce/duration
                      :default 400]
           :path [:setter lifecycle/dynamic]
           :orientation [:setter lifecycle/scalar :coerce coerce-orientation
                         :default :none])))

(def path-transition-lifecycle
  (composite/describe PathTransition
    :ctor []
    :prop-order {:status 1}
    :props path-transition-props))

;; FadeTransition

(def fade-transition-props
  (merge transition-props
         (composite/props
           FadeTransition
           :node [:setter lifecycle/dynamic]
           :duration [:setter lifecycle/scalar :coerce coerce/duration
                      :default 400]
           :from-value [:setter lifecycle/scalar :coerce double :default ##NaN]
           :to-value [:setter lifecycle/scalar :coerce double :default ##NaN]
           :by-value [:setter lifecycle/scalar :coerce double])))

(def fade-transition-lifecycle
  (composite/describe FadeTransition
    :ctor []
    :prop-order {:status 1}
    :props fade-transition-props))

;; FillTransition

(def fill-transition-props
  (merge transition-props
         (composite/props
           FillTransition
           :shape [:setter lifecycle/dynamic]
           :duration [:setter lifecycle/scalar :coerce coerce/duration
                      :default 400]
           :from-value [:setter lifecycle/scalar :coerce coerce/color :default nil]
           :to-value [:setter lifecycle/scalar :coerce coerce/color :default nil])))

(def fill-transition-lifecycle
  (composite/describe FillTransition
    :ctor []
    :prop-order {:status 1}
    :props fill-transition-props))

;; StrokeTransition

(def stroke-transition-props
  (merge transition-props
         (composite/props
           StrokeTransition
           :shape [:setter lifecycle/dynamic]
           :duration [:setter lifecycle/scalar :coerce coerce/duration
                      :default 400]
           :from-value [:setter lifecycle/scalar :coerce coerce/color :default nil]
           :to-value [:setter lifecycle/scalar :coerce coerce/color :default nil])))

(def stroke-transition-lifecycle
  (composite/describe StrokeTransition
    :ctor []
    :prop-order {:status 1}
    :props stroke-transition-props))


;; ScaleTransition

(def scale-transition-props
  (merge transition-props
         (composite/props
           ScaleTransition
           :node [:setter lifecycle/dynamic]
           :duration [:setter lifecycle/scalar :coerce coerce/duration
                      :default 400]
           :from-x [:setter lifecycle/scalar :coerce double :default ##NaN]
           :from-y [:setter lifecycle/scalar :coerce double :default ##NaN]
           :from-z [:setter lifecycle/scalar :coerce double :default ##NaN]
           :to-x [:setter lifecycle/scalar :coerce double :default ##NaN]
           :to-y [:setter lifecycle/scalar :coerce double :default ##NaN]
           :to-z [:setter lifecycle/scalar :coerce double :default ##NaN]
           :by-x [:setter lifecycle/scalar :coerce double]
           :by-y [:setter lifecycle/scalar :coerce double]
           :by-z [:setter lifecycle/scalar :coerce double])))

(def scale-transition-lifecycle
  (composite/describe ScaleTransition
    :ctor []
    :prop-order {:status 1}
    :props scale-transition-props))

;; PauseTransition

(def pause-transition-props
  (merge transition-props
         (composite/props
           PauseTransition
           :duration [:setter lifecycle/scalar :coerce coerce/duration
                      :default 400])))

(def pause-transition-lifecycle
  (composite/describe PauseTransition
    :ctor []
    :prop-order {:status 1}
    :props pause-transition-props))


;; Timeline

(def timeline-props
  (merge animation-props
         (composite/props
           Timeline
           ; cannot alter while playing
           :key-frames [:list lifecycle/scalar :coerce #(map coerce-key-frame %)])))

(def timeline-lifecycle
  (->
    (composite/describe Timeline
      :ctor []
      :prop-order {:status 1}
      :props timeline-props)
    (lifecycle/wrap-on-delete
      #(.stop ^Timeline %1))))

;; keyword->lifecycle

(def keyword->lifecycle
  {::animation-timer animation-timer-lifecycle
   ::translate-transition translate-transition-lifecycle
   ::rotate-transition rotate-transition-lifecycle
   ::parallel-transition parallel-transition-lifecycle
   ::sequential-transition sequential-transition-lifecycle
   ::path-transition path-transition-lifecycle
   ::fade-transition fade-transition-lifecycle
   ::fill-transition fill-transition-lifecycle
   ::pause-transition pause-transition-lifecycle
   ::scale-transition scale-transition-lifecycle
   ::stroke-transition stroke-transition-lifecycle
   ::timeline timeline-lifecycle})

;;;;;;;;;;;;;;;
;; Start app ;;
;;;;;;;;;;;;;;;

(defn init-state []
  {})

(declare *state renderer)

(when (and (.hasRoot #'*state)
           (.hasRoot #'renderer))
  (fx/unmount-renderer *state renderer)
  (reset! *state (init-state)))

(def *state
  (atom (init-state)))

(defn- let-refs [refs desc]
  {:fx/type fx/ext-let-refs
   :refs refs
   :desc desc})

(defn- get-ref [ref]
  {:fx/type fx/ext-get-ref
   :ref ref})

(defn animate-entrance-desc [{:keys [desc]}]
  {:pre [desc]}
  (let-refs {:node desc}
    (let-refs {:animation1
               {:fx/type ::parallel-transition
                :node (get-ref :node)
                :auto-reverse true
                :cycle-count :indefinite
                :status :running
                :children [; rotate 180 degrees
                           {:fx/type ::rotate-transition
                            :node (get-ref :node)
                            :duration [0.5 :s]
                            :from-angle 0
                            :to-angle 185
                            :interpolator :ease-both}
                           ; while also moving left to right
                           {:fx/type ::translate-transition
                            :node (get-ref :node)
                            :duration [0.5 :s]
                            :from-x 0
                            :to-x 75
                            :interpolator :ease-both}]}}
      (get-ref :node))))

(defn timeline [{:keys [desc ^Node instance]}]
  {:pre [desc]}
  (let-refs {:node {:fx/type fx/ext-on-instance-lifecycle
                    :on-created (fn [n]
                                  (swap! *state assoc :instance n))
                    :on-advanced (fn [_ n]
                                   (swap! *state assoc :instance n))
                    :on-deleted (fn [n]
                                  (swap! *state update :instance #(when (= n %) %)))
                    :desc desc}}
    (let-refs (when instance
                {:timeline {:fx/type ::sequential-transition
                            :node (get-ref :node)
                            :cycle-count :indefinite
                            :auto-reverse true
                            :status :running
                            :children
                            [{:fx/type ::timeline
                              :key-frames [{:time [2 :s]
                                            :values [[(.translateXProperty instance)
                                                      75]]}
                                           {:time [0.5 :s]
                                            :values [[(.translateYProperty instance)
                                                      50]]}]}
                             {:fx/type ::pause-transition
                              :duration [0.1 :s]}
                             {:fx/type ::timeline
                              :key-frames [{:time [2 :s]
                                            :values [[(.translateYProperty instance)
                                                      -33]]}
                                           {:time [1 :s]
                                            :values [[(.translateYProperty instance)
                                                      50]]}]}]}})
      (get-ref :node))))

(defn path-transition [{:keys [desc]}]
  (let-refs {:node desc}
    (let-refs {:path {:fx/type ::path-transition
                      :node (get-ref :node)
                      :path {:fx/type :circle
                             :radius 35}
                      :duration [1 :s]
                      :orientation :orthogonal-to-tanget
                      :cycle-count :indefinite
                      :status :running}}
      (get-ref :node))))

(defn fade-transition [{:keys [desc]}]
  (let-refs {:node desc}
    (let-refs {:fade {:fx/type ::fade-transition
                      :node (get-ref :node)
                      :from-value 1.0
                      :to-value 0.3
                      :cycle-count :indefinite
                      :duration [1 :s]
                      :auto-reverse true
                      :status :running}}
      (get-ref :node))))

(defn fill-transition [{:keys [desc]}]
  (let-refs {:shape desc}
    (let-refs {:fade {:fx/type ::fill-transition
                      :shape (get-ref :shape)
                      :from-value :green
                      :to-value :red
                      :cycle-count :indefinite
                      :duration [1 :s]
                      :auto-reverse true
                      :status :running}}
      (get-ref :shape))))

(defn scale-transition [{:keys [desc]}]
  (let-refs {:node desc}
    (let-refs {:fade {:fx/type ::scale-transition
                      :node (get-ref :node)
                      :by-x 1
                      :by-y 5
                      :cycle-count :indefinite
                      :duration [1 :s]
                      :auto-reverse true
                      :status :running}}
      (get-ref :node))))

(defn stroke-transition [{:keys [desc]}]
  (let-refs {:shape desc}
    (let-refs {:fade {:fx/type ::stroke-transition
                      :shape (get-ref :shape)
                      :from-value :red
                      :to-value :blue
                      :cycle-count :indefinite
                      :duration [1 :s]
                      :auto-reverse true
                      :status :running}}
      (get-ref :shape))))

(defn grow-shrink [current limit]
  (if (= current -1)
    ; start at the largest size to inform parent
    limit
    (if (even? (int (/ current limit)))
      (mod current limit)
      (- limit (mod current limit)))))

(defn with-header [text row col desc]
  {:fx/type :h-box
   :fill-height true
   :grid-pane/row row
   :grid-pane/column col
   :spacing 10
   :children [{:fx/type :label
               :text text}
              desc]})

(defn view [{{:keys [timer-duration instance] :or {timer-duration -1}} :state}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 600
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type :grid-pane
                  :row-constraints (repeat 4 {:fx/type :row-constraints
                                              :percent-height 100/4})
                  :column-constraints (repeat 2 {:fx/type :column-constraints
                                                 :percent-width 100/2})
                  :children [(with-header
                               "Parallel rotate+translate"
                               0 0
                               {:fx/type animate-entrance-desc
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 100}})
                             (with-header
                               "Animation timer"
                               1 0
                               (let [max-width 200
                                     max-height 100]
                                 (let-refs
                                   {:timer {:fx/type ::animation-timer
                                            :handler {:event/type ::animation-timer}
                                            :state :starting}}
                                   {:fx/type :rectangle
                                    :width (grow-shrink timer-duration max-width)
                                    :height (grow-shrink timer-duration max-height)})))
                             (with-header
                               "Sequential Timelines"
                               2 0
                               {:fx/type timeline
                                :instance instance
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 100}})
                             (with-header
                               "Path transition"
                               3 0
                               {:fx/type path-transition
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 100}})
                             (with-header
                               "Fade transition"
                               0 1
                               {:fx/type fade-transition
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 100}})
                             (with-header
                               "Fill transition"
                               1 1
                               {:fx/type fill-transition
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 100}})
                             (with-header
                               "Scale transition"
                               2 1
                               {:fx/type scale-transition
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 15}})
                             (with-header
                               "Stroke transition"
                               3 1
                               {:fx/type stroke-transition
                                :desc {:fx/type :rectangle
                                       :width 50
                                       :height 100}})]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type view
                                     :state state}))
    :opts {:fx.opt/type->lifecycle (some-fn keyword->lifecycle 
                                            fx/keyword->lifecycle
                                            fx/fn->lifecycle)
           :fx.opt/map-event-handler
           #(case (:event/type %)
              ::animation-timer
              (swap! *state update :timer-duration (fnil inc 0))
              )}))

(fx/mount-renderer *state renderer)
