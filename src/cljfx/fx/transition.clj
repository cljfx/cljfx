(ns cljfx.fx.transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.animation :as fx.animation])
  (:import [javafx.animation Transition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.animation/props
    (composite/props Transition
      :interpolator [:setter lifecycle/scalar :coerce coerce/interpolator :default :ease-in])))
