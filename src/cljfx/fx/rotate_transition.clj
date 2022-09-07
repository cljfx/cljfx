(ns cljfx.fx.rotate-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation RotateTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props RotateTransition
      :axis [:setter lifecycle/scalar :coerce coerce/point-3d]
      :by-angle [:setter lifecycle/scalar :coerce double :default 0.0]
      :duration [:setter lifecycle/scalar :coerce coerce/duration :default 400]
      :from-angle [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-angle [:setter lifecycle/scalar :coerce double :default ##NaN]
      :node [:setter lifecycle/dynamic])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe RotateTransition
      :ctor []
      :prop-order {:status 1}
      :props props)
    :rotate-transition))
