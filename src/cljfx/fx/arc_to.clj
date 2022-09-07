(ns cljfx.fx.arc-to
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape ArcTo]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.path-element/props
    (composite/props ArcTo
      :radius-x [:setter lifecycle/scalar :coerce double :default 0]
      :radius-y [:setter lifecycle/scalar :coerce double :default 0]
      :x-axis-rotation [:setter lifecycle/scalar :coerce double :default 0]
      :large-arc-flag [:setter lifecycle/scalar :default false]
      :sweep-flag [:setter lifecycle/scalar :default false]
      :x [:setter lifecycle/scalar :coerce double :default 0]
      :y [:setter lifecycle/scalar :coerce double :default 0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe ArcTo
      :ctor []
      :props props)
    :arc-to))
