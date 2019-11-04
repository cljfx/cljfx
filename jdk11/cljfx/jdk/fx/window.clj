(ns cljfx.jdk.fx.window
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.stage Window]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Window
    :force-integer-render-scale [:setter lifecycle/scalar :default false]
    :render-scale-x [:setter lifecycle/scalar :coerce double :default 1]
    :render-scale-y [:setter lifecycle/scalar :coerce double :default 1]))