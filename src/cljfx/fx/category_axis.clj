(ns cljfx.fx.category-axis
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.axis :as fx.axis])
  (:import [javafx.scene.chart CategoryAxis]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.axis/props
    (lifecycle.composite/props CategoryAxis
      :categories [:list lifecycle/scalar]
      :start-margin [:setter lifecycle/scalar :coerce double :default 5.0]
      :end-margin [:setter lifecycle/scalar :coerce double :default 5.0]
      :gap-start-and-end [:setter lifecycle/scalar :default true])))

(def lifecycle
  (lifecycle.composite/describe CategoryAxis
    :ctor []
    :props props))
