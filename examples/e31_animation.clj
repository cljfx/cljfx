(ns e31-animation
  (:require [cljfx.api :as fx]
            [cljfx.prop :as prop]
            [cljfx.composite :as composite]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.component :as component])
  (:import [java.util Collection]
           [javafx.event EventHandler]
           [javafx.scene Node]
           [javafx.scene.transform Transform]
           [javafx.animation TranslateTransition Interpolator Transition Timeline]
           [javafx.util Duration]
           [javafx.scene.layout Region]
           [javafx.beans.value WritableValue]
           [javafx.animation AnimationTimer Animation ParallelTransition RotateTransition
            PathTransition$OrientationType
            FadeTransition StrokeTransition FillTransition PauseTransition
            ScaleTransition
            KeyFrame KeyValue SequentialTransition PathTransition]))

(set! *warn-on-reflection* true)

;;;;;;;;;;;
;; Notes ;;
;;;;;;;;;;;

; This file runs a bunch of animations when evaluated.
; The `Demo` section below contains the source for those
; animations. It uses the below implementation,
; in particular the `keyword->lifecycle` map in this namespace.

; This is a first crack at animation with cljfx. It's mostly
; as you might expect, using implementing lifecycles for the
; various packages javafx.animation classes, but here are some
; notes on the more interesting parts.

; `Timeline`s take KeyFrames which must be passed `WritableValue`s,
; which means things like (.translateXProperty node) and (.translateXProperty node)
; in practice. This is somewhat awkward to do with descs.
; To get a property out of a desc, I added the
; lifecycle ::coerce, which takes a :desc and a :coerce function
; that is called on the result of `component/instance`.
;   eg., the component created from desc
;          {:fx/type ::coerce
;           :desc {:fx/type :rectangle ...}
;           :coerce #(.translateYProperty ^Node %)}
;        returns the .translateYProperty as the `component/instance`.

; This is particularly effective when combined with let-refs, so you
; can declare the node beforehand and extract what Properties you need
; as you go. See the `timeline` function in this namespace for example
; usage.

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
        #(try (.stop ^AnimationTimer %)
              (catch IllegalStateException _)))))


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

(defn wrap-coerce-with-prop [lifecycle child-prop coerce-prop]
  (with-meta
    [::coerce-with-prop lifecycle child-prop coerce-prop]
    {`lifecycle/create (fn [_ desc opts]
                         (let [child-desc (get desc child-prop)
                               child (lifecycle/create lifecycle child-desc opts)
                               coerce (get desc coerce-prop)]
                           (when-not (fn? coerce)
                             (throw (ex-info "Coercion must be fn"
                                             {:coerce-prop coerce-prop
                                              :coerce coerce})))
                           (with-meta {:child child
                                       :coerce coerce
                                       :value (coerce (component/instance child))}
                                      {`component/instance :value})))
     `lifecycle/advance (fn [_ component desc opts]
                          (let [old-coerce (:coerce component)
                                new-coerce (get desc coerce-prop)
                                child (:child component)
                                old-instance (component/instance child)
                                child-desc (get desc child-prop)
                                new-child (lifecycle/advance lifecycle child child-desc opts)
                                new-instance (component/instance new-child)]
                            (when-not (fn? new-coerce)
                              (throw (ex-info "Coercion must be fn"
                                              {:coerce-prop coerce-prop
                                               :coerce new-coerce})))
                            (cond-> component
                              :always
                              (assoc :child new-child)

                              (or (not= old-instance new-instance)
                                  (not= old-coerce new-coerce))
                              (assoc :value (new-coerce new-instance)))))
     `lifecycle/delete (fn [_ component opts]
                         (lifecycle/delete lifecycle (:child component) opts))}))

(def coerce-lifecycle
  (wrap-coerce-with-prop lifecycle/dynamic :desc :coerce))

(def key-value-props
  {:target (prop/make mutator/forbidden
                      (lifecycle/if-desc #(instance? WritableValue %)
                        lifecycle/scalar
                        lifecycle/dynamic))
   :end-value (prop/make mutator/forbidden
                         lifecycle/scalar)
   ; :default and :coerce handled in :ctor because :interpolator is immutable
   :interpolator (prop/make mutator/forbidden
                            lifecycle/scalar)})

(def key-value-lifecycle
  (composite/lifecycle
    {:ctor (fn [target end-value interpolator]
             (KeyValue. target
                        end-value
                        (if interpolator
                          (coerce-interpolator interpolator)
                          Interpolator/LINEAR)))
     :args [:target :end-value :interpolator]
     :props key-value-props}))

(def key-frame-props
  {:time (prop/make mutator/forbidden
                    lifecycle/scalar
                    :coerce coerce/duration)
   :name (prop/make mutator/forbidden
                    lifecycle/scalar
                    :default nil)
   :on-finished (prop/make mutator/forbidden
                           lifecycle/event-handler
                           :default nil)
   :values (prop/make mutator/forbidden
                      (lifecycle/wrap-many
                        ;TODO allow vector case
                        (lifecycle/if-desc #(instance? KeyValue %)
                          lifecycle/scalar
                          (lifecycle/if-desc #(and (map? %)
                                                   (contains? % :fx/type))
                            lifecycle/dynamic
                            key-value-lifecycle))))})

(def key-frame-lifecycle
  (composite/lifecycle
    {:ctor (fn [^Duration time
                ^String name
                ^EventHandler on-finished
                ^Collection values]
             (KeyFrame. time name on-finished values))
     :args [:time :name :on-finished :values]
     :props key-frame-props}))

(def timeline-props
  (merge animation-props
         (composite/props
           Timeline
           ; Future syntax ideas for key-frames
           ; eg. At 2 sec, move node's x prop to 30
           ;     {[2 :s] [(get-ref :node-x) 30}
           ;
           ; (defalias KeyFrameKey
           ;    (U TimeDesc
           ;       (HMap :mandatory {:time TimeDesc}
           ;             :optional {:interpolator InterpolatorDesc
           ;                        :name Str
           ;                        :on-finished EventHandlerDesc}
           ;             :absent-keys #{:fx/type})))
           ; (defalias KeyFrameValue
           ;   (U '[Desc Any]
           ;      '[Desc Any Interpolator]
           ;      (HMap :mandatory {:target Desc}
           ;            :optional {:end-value Any
           ;                       :interpolator Interpolator}
           ;            :absent-keys #{:fx/type})))
           ; (defalias KeyFrameDesc
           ;   (U javafx.animation.KeyFrame
           ;      '[KeyFrameKey (U KeyFrameValue (Vec KeyFrameValue))]
           ;      (HMap :mandatory {:time TimeDesc}
           ;            :optional {:values KeyFrameValues}
           ;            :absent-keys #{:fx/type})))
           ; (defalias KeyFrameValues
           ;   (Seqable KeyFrameDesc))
           :key-frames [:list (lifecycle/wrap-many
                                (lifecycle/if-desc #(instance? KeyFrame %)
                                  lifecycle/scalar
                                  ; reserve Descs for now
                                  (lifecycle/if-desc #(and (map? %)
                                                           (contains? % :fx/type))
                                    lifecycle/dynamic
                                    key-frame-lifecycle)))])))

(def timeline-lifecycle
  (->
    (composite/describe Timeline
      :ctor []
      :prop-order {:status 1}
      :props timeline-props)
    (lifecycle/wrap-on-delete
      #(try (.stop ^Timeline %)
            ; cannot stop children in nested animations
            (catch IllegalStateException _)))))

;; keyword->lifecycle

(def keyword->lifecycle
  {::coerce coerce-lifecycle
   ::animation-timer animation-timer-lifecycle
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

;;;;;;;;;;
;; Demo ;;
;;;;;;;;;;

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

(defn translate-x-property [^Node instance]
  (.translateXProperty instance))

(defn translate-y-property [^Node instance]
  (.translateYProperty instance))

(defn timeline [{:keys [desc]}]
  {:pre [desc]}
  (let-refs {:node desc}
    (let-refs {:node-x {:fx/type ::coerce
                        :desc (get-ref :node)
                        :coerce translate-x-property}
               :node-y {:fx/type ::coerce
                        :desc (get-ref :node)
                        :coerce translate-y-property}}
      (let-refs {:timeline {:fx/type ::sequential-transition
                            :node (get-ref :node)
                            :cycle-count :indefinite
                            :auto-reverse true
                            :status :running
                            :children
                            [{:fx/type ::timeline
                              :key-frames 
                              #{{:time [2 :s]
                                 :values [{:target (get-ref :node-x)
                                           :end-value 30}]}
                                {:time [0.5 :s]
                                 :values [{:target (get-ref :node-y)
                                           :end-value 50}]}
                                {:time [1 :s]
                                 :values [{:target (get-ref :node-y)
                                           :end-value 0}]}}}
                             {:fx/type ::pause-transition
                              :duration [0.4 :s]}
                             {:fx/type ::timeline
                              :key-frames #{{:time [2 :s]
                                             :values [{:target (get-ref :node-y)
                                                       :end-value 0}]}
                                            {:time [1 :s]
                                             :values [{:target (get-ref :node-y)
                                                       :end-value 50}]}}}]}}
        (get-ref :node)))))

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
                      :to-value :yellow
                      :cycle-count :indefinite
                      :duration [0.5 :s]
                      :auto-reverse true
                      :status :running}}
      (get-ref :shape))))

(defn grow-shrink [current limit]
  (if (even? (int (/ current limit)))
    (mod current limit)
    (- limit (mod current limit))))

(defn with-header [text row col desc]
  {:fx/type :h-box
   :fill-height true
   :grid-pane/row row
   :grid-pane/column col
   :spacing 10
   :children [{:fx/type :label
               :text text}
              desc]})

(defn view [{{:keys [timer-duration] :or {timer-duration 0}} :state}]
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
                                       :stroke-width 10
                                       :arc-height 20
                                       :arc-width 20
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
