(ns cljfx.fx.axis
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.region :as fx.region])
  (:import [javafx.scene.chart Axis]
           [javafx.geometry Side]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Axis
    :extends [fx.region/lifecycle]
    :props {:side [:setter lifecycle/scalar :coerce (coerce/enum Side)]
            :label [:setter lifecycle/scalar]
            :tick-mark-visible [:setter lifecycle/scalar :default true]
            :tick-labels-visible [:setter lifecycle/scalar :default true]
            :tick-length [:setter lifecycle/scalar :coerce double :default 8]
            :auto-ranging [:setter lifecycle/scalar :default true]
            :tick-label-font [:setter lifecycle/scalar :coerce coerce/font
                              :default {:family "System" :size 8}]
            :tick-label-fill [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :tick-label-gap [:setter lifecycle/scalar :coerce double :default 3]
            :animated [:setter lifecycle/scalar :default true]
            :tick-label-rotation [:setter lifecycle/scalar :coerce double :default 0]}))