(ns cljfx.fx.path-transition
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.transition :as fx.transition])
  (:import [javafx.animation PathTransition]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.transition/props
    (composite/props PathTransition
      :node [:setter lifecycle/dynamic]
      :duration [:setter lifecycle/scalar :coerce coerce/duration :default 400]
      :path [:setter lifecycle/dynamic]
      :orientation [:setter lifecycle/scalar :coerce coerce/path-transition-orientation :default :none])))

(def lifecycle
  (composite/describe PathTransition
    :ctor []
    :prop-order {:status 1}
    :props props))
