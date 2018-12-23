(ns cljfx.fx.shape
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce])
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
    :props {:fill [:setter prop/scalar :coerce coerce/paint :default :black]
            :stroke [:setter prop/scalar :coerce coerce/paint]
            :smooth [:setter prop/scalar :default true]
            :stroke-dash-array [:list prop/scalar :coerce (fn [x _] (map double x))]
            :stroke-dash-offset [:setter prop/scalar :coerce coerce/as-double :default 0]
            :stroke-line-cap [:setter prop/scalar
                              :coerce (coerce/enum StrokeLineCap)
                              :default :square]
            :stroke-line-join [:setter prop/scalar :coerce (coerce/enum StrokeLineJoin)
                               :default :miter]
            :stroke-miter-limit [:setter prop/scalar :coerce coerce/as-double :default 10]
            :stroke-type [:setter prop/scalar :coerce (coerce/enum StrokeType)
                          :default :centered]
            :stroke-width [:setter prop/scalar :coerce coerce/as-double :default 1]}))

(def path-element
  (lifecycle.composite/describe PathElement
    :props {:absolute [:setter prop/scalar :default true]}))

(def arc
  (lifecycle.composite/describe Arc
    :ctor []
    :extends [shape]
    :props {:center-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :center-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :length [:setter prop/scalar :coerce coerce/as-double :default 0]
            :radius-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :radius-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :start-angle [:setter prop/scalar :coerce coerce/as-double :default 0]
            :type [:setter prop/scalar :coerce (coerce/enum ArcType) :default :open]}))

(def circle
  (lifecycle.composite/describe Circle
    :ctor []
    :extends [shape]
    :props {:center-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :center-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :radius [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def cubic-curve
  (lifecycle.composite/describe CubicCurve
    :ctor []
    :extends [shape]
    :props {:control-x1 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-x2 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-y1 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-y2 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :end-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :end-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :start-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :start-y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def ellipse
  (lifecycle.composite/describe Ellipse
    :ctor []
    :extends [shape]
    :props {:center-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :center-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :radius-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :radius-y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def line
  (lifecycle.composite/describe Line
    :ctor []
    :extends [shape]
    :props {:fill [:setter prop/scalar :coerce coerce/paint]
            :stroke [:setter prop/scalar :coerce coerce/paint :default :black]
            :start-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :start-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :end-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :end-y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def path
  (lifecycle.composite/describe Path
    :ctor []
    :extends [shape]
    :default-prop [:elements prop/extract-all]
    :props {:elements [:list prop/component-vec]
            :fill [:setter prop/scalar :coerce coerce/paint]
            :stroke [:setter prop/scalar :coerce coerce/paint :default :black]
            :fill-rule [:setter prop/scalar :coerce (coerce/enum FillRule)
                        :default :non-zero]}))

(def arc-to
  (lifecycle.composite/describe ArcTo
    :ctor []
    :extends [path-element]
    :props {:radius-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :radius-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :x-axis-rotation [:setter prop/scalar :coerce coerce/as-double :default 0]
            :large-arc-flag [:setter prop/scalar :default false]
            :sweep-flag [:setter prop/scalar :default false]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def close-path
  (lifecycle.composite/describe ClosePath
    :ctor []
    :extends [path-element]))

(def cubic-curve-to
  (lifecycle.composite/describe CubicCurveTo
    :ctor []
    :extends [path-element]
    :props {:control-x1 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-x2 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-y1 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-y2 [:setter prop/scalar :coerce coerce/as-double :default 0]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def h-line-to
  (lifecycle.composite/describe HLineTo
    :ctor []
    :extends [path-element]
    :default-prop [:x prop/extract-single]
    :props {:x [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def line-to (lifecycle.composite/describe LineTo
               :ctor []
               :extends [path-element]
               :props {:x [:setter prop/scalar :coerce coerce/as-double :default 0]
                       :y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def move-to (lifecycle.composite/describe MoveTo
               :ctor []
               :extends [path-element]
               :props {:x [:setter prop/scalar :coerce coerce/as-double :default 0]
                       :y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def quad-curve-to
  (lifecycle.composite/describe QuadCurveTo
    :ctor []
    :extends [path-element]
    :props {:control-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :control-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def v-line-to (lifecycle.composite/describe VLineTo
                 :ctor []
                 :extends [path-element]
                 :default-prop [:y prop/extract-single]
                 :props {:y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def polygon (lifecycle.composite/describe Polygon
               :ctor []
               :extends [shape]
               :default-prop [:points prop/extract-all]
               :props {:points [:list prop/scalar :coerce (fn [x _]
                                                            (->> x
                                                                 (mapcat identity)
                                                                 (map double)))]}))
(def polyline
  (lifecycle.composite/describe Polyline
    :ctor []
    :extends [shape]
    :default-prop [:points prop/extract-all]
    :props {:fill [:setter prop/scalar :coerce coerce/paint]
            :stroke [:setter prop/scalar :coerce coerce/paint :default :black]
            :points [:list prop/scalar :coerce (fn [x _]
                                                 (->> x
                                                      (mapcat identity)
                                                      (map double)))]}))
(def quad-curve
  (lifecycle.composite/describe QuadCurve
    :ctor []
    :extends [shape]
    :props {:control-x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :control-y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :end-x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :end-y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :start-x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :start-y [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def rectangle
  (lifecycle.composite/describe Rectangle
    :ctor []
    :extends [shape]
    :props {:arc-height [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :arc-width [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :height [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :width [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def svg-path
  (lifecycle.composite/describe SVGPath
    :ctor []
    :extends [shape]
    :default-prop [:content prop/extract-single]
    :props {:content [:setter prop/scalar]
            :fill-rule [:setter prop/scalar
                        :coerce (coerce/enum FillRule)
                        :default :non-zero]}))

(def text
  (lifecycle.composite/describe Text
    :ctor []
    :extends [shape]
    :default-prop [:text prop/extract-single]
    :props {:text [:setter prop/scalar :default ""]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :font [:setter prop/scalar :coerce coerce/font]
            :text-origin [:setter prop/scalar :coerce (coerce/enum VPos)
                          :default :baseline]
            :bounds-type [:setter prop/scalar :coerce (coerce/enum TextBoundsType)
                          :default :logical]
            :wrapping-width [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :underline [:setter prop/scalar :default false]
            :strikethrough [:setter prop/scalar :default false]
            :text-alignment [:setter prop/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]
            :line-spacing [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :font-smoothing-type [:setter prop/scalar
                                  :coerce (coerce/enum FontSmoothingType)
                                  :default :gray]
            :selection-start [:setter prop/scalar :coerce coerce/as-int :default -1]
            :selection-end [:setter prop/scalar :coerce coerce/as-int :default -1]
            :selection-fill [:setter prop/scalar :coerce coerce/paint :default :white]
            :caret-position [:setter prop/scalar :coerce coerce/as-int :default -1]
            :caret-bias [:setter prop/scalar :default true]}))

(def tag->lifecycle
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
