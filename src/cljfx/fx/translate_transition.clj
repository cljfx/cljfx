(ns cljfx.fx.translate-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation TranslateTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props TranslateTransition
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

(def lifecycle
  (composite/describe TranslateTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
