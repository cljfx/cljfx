(ns cljfx.fx.shape
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene.shape Shape StrokeLineCap StrokeLineJoin StrokeType]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Shape
    :extends [fx.node/lifecycle]
    :props {:fill [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint]
            :smooth [:setter lifecycle/scalar :default true]
            :stroke-dash-array [:list lifecycle/scalar :coerce #(map double %)]
            :stroke-dash-offset [:setter lifecycle/scalar :coerce double :default 0]
            :stroke-line-cap [:setter lifecycle/scalar
                              :coerce (coerce/enum StrokeLineCap)
                              :default :square]
            :stroke-line-join [:setter lifecycle/scalar :coerce (coerce/enum StrokeLineJoin)
                               :default :miter]
            :stroke-miter-limit [:setter lifecycle/scalar :coerce double :default 10]
            :stroke-type [:setter lifecycle/scalar :coerce (coerce/enum StrokeType)
                          :default :centered]
            :stroke-width [:setter lifecycle/scalar :coerce double :default 1]}))
