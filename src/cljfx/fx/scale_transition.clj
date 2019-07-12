(ns cljfx.fx.scale-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation ScaleTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props ScaleTransition
      :node [:setter lifecycle/dynamic]
      :duration [:setter lifecycle/scalar :coerce coerce/duration :default 400]
      :from-x [:setter lifecycle/scalar :coerce double :default ##NaN]
      :from-y [:setter lifecycle/scalar :coerce double :default ##NaN]
      :from-z [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-x [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-y [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-z [:setter lifecycle/scalar :coerce double :default ##NaN]
      :by-x [:setter lifecycle/scalar :coerce double]
      :by-y [:setter lifecycle/scalar :coerce double]
      :by-z [:setter lifecycle/scalar :coerce double])))

(def lifecycle
  (composite/describe ScaleTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
