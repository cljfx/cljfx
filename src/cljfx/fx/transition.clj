(ns cljfx.fx.transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.animation :as fx.animation])
  (:import [javafx.animation Transition Interpolator]))

(set! *warn-on-reflection* true)

(defn coerce-interpolator [x]
  (cond
    (instance? Interpolator x) x
    (vector? x) (case (nth x 0)
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

(def props
  (merge
    fx.animation/props
    (composite/props Transition
      :interpolator [:setter lifecycle/scalar :coerce coerce-interpolator :default :ease-both])))
