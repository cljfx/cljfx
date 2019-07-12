(ns cljfx.fx.stroke-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation StrokeTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props StrokeTransition
      :shape [:setter lifecycle/dynamic]
      :duration [:setter lifecycle/scalar :coerce coerce/duration :default 400]
      :from-value [:setter lifecycle/scalar :coerce coerce/color :default nil]
      :to-value [:setter lifecycle/scalar :coerce coerce/color :default nil])))

(def lifecycle
  (composite/describe StrokeTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
