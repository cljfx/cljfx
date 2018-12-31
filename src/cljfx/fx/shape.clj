(ns cljfx.fx.shape
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.shape Arc ArcType Circle CubicCurve Ellipse Line Path ArcTo
                               FillRule ClosePath CubicCurveTo HLineTo LineTo MoveTo
                               QuadCurveTo VLineTo Rectangle QuadCurve Polyline Polygon
                               SVGPath Shape StrokeLineCap StrokeLineJoin StrokeType
                               PathElement]
           [javafx.scene.text Text TextBoundsType TextAlignment FontSmoothingType]
           [javafx.geometry VPos]))

(set! *warn-on-reflection* true)

(def shape
  (lifecycle.composite/describe Shape
    :extends [fx.scene/node]
    :props {:fill [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint]
            :smooth [:setter lifecycle/scalar :default true]
            :stroke-dash-array [:list lifecycle/scalar :coerce (fn [x _] (map double x))]
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

(def path-element
  (lifecycle.composite/describe PathElement
    :props {:absolute [:setter lifecycle/scalar :default true]}))

(def arc
  (lifecycle.composite/describe Arc
    :ctor []
    :extends [shape]
    :props {:center-x [:setter lifecycle/scalar :coerce double :default 0]
            :center-y [:setter lifecycle/scalar :coerce double :default 0]
            :length [:setter lifecycle/scalar :coerce double :default 0]
            :radius-x [:setter lifecycle/scalar :coerce double :default 0]
            :radius-y [:setter lifecycle/scalar :coerce double :default 0]
            :start-angle [:setter lifecycle/scalar :coerce double :default 0]
            :type [:setter lifecycle/scalar :coerce (coerce/enum ArcType) :default :open]}))

(def circle
  (lifecycle.composite/describe Circle
    :ctor []
    :extends [shape]
    :props {:center-x [:setter lifecycle/scalar :coerce double :default 0]
            :center-y [:setter lifecycle/scalar :coerce double :default 0]
            :radius [:setter lifecycle/scalar :coerce double :default 0]}))

(def cubic-curve
  (lifecycle.composite/describe CubicCurve
    :ctor []
    :extends [shape]
    :props {:control-x1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-x2 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y2 [:setter lifecycle/scalar :coerce double :default 0]
            :end-x [:setter lifecycle/scalar :coerce double :default 0]
            :end-y [:setter lifecycle/scalar :coerce double :default 0]
            :start-x [:setter lifecycle/scalar :coerce double :default 0]
            :start-y [:setter lifecycle/scalar :coerce double :default 0]}))

(def ellipse
  (lifecycle.composite/describe Ellipse
    :ctor []
    :extends [shape]
    :props {:center-x [:setter lifecycle/scalar :coerce double :default 0]
            :center-y [:setter lifecycle/scalar :coerce double :default 0]
            :radius-x [:setter lifecycle/scalar :coerce double :default 0]
            :radius-y [:setter lifecycle/scalar :coerce double :default 0]}))

(def line
  (lifecycle.composite/describe Line
    :ctor []
    :extends [shape]
    :props {:fill [:setter lifecycle/scalar :coerce coerce/paint]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :start-x [:setter lifecycle/scalar :coerce double :default 0]
            :start-y [:setter lifecycle/scalar :coerce double :default 0]
            :end-x [:setter lifecycle/scalar :coerce double :default 0]
            :end-y [:setter lifecycle/scalar :coerce double :default 0]}))

(def path
  (lifecycle.composite/describe Path
    :ctor []
    :extends [shape]
    :props {:elements [:list lifecycle/dynamics]
            :fill [:setter lifecycle/scalar :coerce coerce/paint]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :fill-rule [:setter lifecycle/scalar :coerce (coerce/enum FillRule)
                        :default :non-zero]}))

(def arc-to
  (lifecycle.composite/describe ArcTo
    :ctor []
    :extends [path-element]
    :props {:radius-x [:setter lifecycle/scalar :coerce double :default 0]
            :radius-y [:setter lifecycle/scalar :coerce double :default 0]
            :x-axis-rotation [:setter lifecycle/scalar :coerce double :default 0]
            :large-arc-flag [:setter lifecycle/scalar :default false]
            :sweep-flag [:setter lifecycle/scalar :default false]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def close-path
  (lifecycle.composite/describe ClosePath
    :ctor []
    :extends [path-element]))

(def cubic-curve-to
  (lifecycle.composite/describe CubicCurveTo
    :ctor []
    :extends [path-element]
    :props {:control-x1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-x2 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y1 [:setter lifecycle/scalar :coerce double :default 0]
            :control-y2 [:setter lifecycle/scalar :coerce double :default 0]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def h-line-to
  (lifecycle.composite/describe HLineTo
    :ctor []
    :extends [path-element]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]}))

(def line-to
  (lifecycle.composite/describe LineTo
    :ctor []
    :extends [path-element]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def move-to
  (lifecycle.composite/describe MoveTo
    :ctor []
    :extends [path-element]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def quad-curve-to
  (lifecycle.composite/describe QuadCurveTo
    :ctor []
    :extends [path-element]
    :props {:control-x [:setter lifecycle/scalar :coerce double :default 0]
            :control-y [:setter lifecycle/scalar :coerce double :default 0]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def v-line-to
  (lifecycle.composite/describe VLineTo
    :ctor []
    :extends [path-element]
    :props {:y [:setter lifecycle/scalar :coerce double :default 0]}))

(def polygon
  (lifecycle.composite/describe Polygon
    :ctor []
    :extends [shape]
    :props {:points [:list lifecycle/scalar :coerce #(->> %
                                                          (mapcat identity)
                                                          (map double))]}))
(def polyline
  (lifecycle.composite/describe Polyline
    :ctor []
    :extends [shape]
    :props {:fill [:setter lifecycle/scalar :coerce coerce/paint]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :points [:list lifecycle/scalar :coerce #(->> %
                                                          (mapcat identity)
                                                          (map double))]}))
(def quad-curve
  (lifecycle.composite/describe QuadCurve
    :ctor []
    :extends [shape]
    :props {:control-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :control-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :end-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :end-y [:setter lifecycle/scalar :coerce double :default 0.0]
            :start-x [:setter lifecycle/scalar :coerce double :default 0.0]
            :start-y [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def rectangle
  (lifecycle.composite/describe Rectangle
    :ctor []
    :extends [shape]
    :props {:arc-height [:setter lifecycle/scalar :coerce double :default 0.0]
            :arc-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :height [:setter lifecycle/scalar :coerce double :default 0.0]
            :width [:setter lifecycle/scalar :coerce double :default 0.0]
            :x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def svg-path
  (lifecycle.composite/describe SVGPath
    :ctor []
    :extends [shape]
    :props {:content [:setter lifecycle/scalar]
            :fill-rule [:setter lifecycle/scalar
                        :coerce (coerce/enum FillRule)
                        :default :non-zero]}))

(def text
  (lifecycle.composite/describe Text
    :ctor []
    :extends [shape]
    :props {:text [:setter lifecycle/scalar :default ""]
            :x [:setter lifecycle/scalar :coerce double :default 0.0]
            :y [:setter lifecycle/scalar :coerce double :default 0.0]
            :font [:setter lifecycle/scalar :coerce coerce/font]
            :text-origin [:setter lifecycle/scalar :coerce (coerce/enum VPos)
                          :default :baseline]
            :bounds-type [:setter lifecycle/scalar :coerce (coerce/enum TextBoundsType)
                          :default :logical]
            :wrapping-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :underline [:setter lifecycle/scalar :default false]
            :strikethrough [:setter lifecycle/scalar :default false]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]
            :line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
            :font-smoothing-type [:setter lifecycle/scalar
                                  :coerce (coerce/enum FontSmoothingType)
                                  :default :gray]
            :selection-start [:setter lifecycle/scalar :coerce int :default -1]
            :selection-end [:setter lifecycle/scalar :coerce int :default -1]
            :selection-fill [:setter lifecycle/scalar :coerce coerce/paint :default :white]
            :caret-position [:setter lifecycle/scalar :coerce int :default -1]
            :caret-bias [:setter lifecycle/scalar :default true]}))

(def keyword->lifecycle
  {:arc arc
   :circle circle
   :cubic-curve cubic-curve
   :ellipse ellipse
   :line line
   :path path
   :path-element/arc-to arc-to
   :path-element/close-path close-path
   :path-element/cubic-curve-to cubic-curve-to
   :path-element/h-line-to h-line-to
   :path-element/line-to line-to
   :path-element/move-to move-to
   :path-element/quad-curve-to quad-curve-to
   :path-element/v-line-to v-line-to
   :polygon polygon
   :polyline polyline
   :quad-curve quad-curve
   :rectangle rectangle
   :svg-path svg-path
   :text text})
