(ns cljfx.fx.pause-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation PauseTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props PauseTransition
      :duration [:setter lifecycle/scalar :coerce coerce/duration :default 400])))

(def lifecycle
  (composite/describe PauseTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
