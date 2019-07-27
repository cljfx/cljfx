(ns cljfx.fx.parallel-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation ParallelTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props ParallelTransition
      :children [:list lifecycle/dynamics]
      :node [:setter lifecycle/dynamic])))

(def lifecycle
  (composite/describe ParallelTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
