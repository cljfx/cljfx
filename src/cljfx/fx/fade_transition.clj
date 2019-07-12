(ns cljfx.fx.fade-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation FadeTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props FadeTransition
      :node [:setter lifecycle/dynamic]
      :duration [:setter lifecycle/scalar :coerce coerce/duration :default 400]
      :from-value [:setter lifecycle/scalar :coerce double :default ##NaN]
      :to-value [:setter lifecycle/scalar :coerce double :default ##NaN]
      :by-value [:setter lifecycle/scalar :coerce double])))

(def lifecycle
  (composite/describe FadeTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
