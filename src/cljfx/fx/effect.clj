(ns cljfx.fx.effect
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect Blend BlendMode Bloom BoxBlur ColorAdjust ColorInput
                                DisplacementMap DropShadow BlurType Light$Spot Light$Point
                                Light$Distant MotionBlur PerspectiveTransform Reflection
                                SepiaTone Shadow GaussianBlur Glow ImageInput InnerShadow
                                Lighting Light]))

(set! *warn-on-reflection* true)

(def light
  (lifecycle.composite/describe Light
    :props {:color [:setter prop/scalar :coerce coerce/color :default :white]}))

(def point-light
  (lifecycle.composite/describe Light$Point
    :ctor []
    :extends [light]
    :props {:x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :z [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def blend
  (lifecycle.composite/describe Blend
    :ctor []
    :props {:bottom-input [:setter prop/component]
            :mode [:setter prop/scalar :coerce (coerce/enum BlendMode)]
            :opacity [:setter prop/scalar :coerce coerce/as-double :default 1]
            :top-input [:setter prop/component]}))

(def bloom
  (lifecycle.composite/describe Bloom
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :threshold [:setter prop/scalar :coerce coerce/as-double :default 0.3]}))

(def box-blur
  (lifecycle.composite/describe BoxBlur
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :iterations [:setter prop/scalar :coerce coerce/as-int :default 1]
            :width [:setter prop/scalar :coerce coerce/as-double :default 5]
            :height [:setter prop/scalar :coerce coerce/as-double :default 5]}))

(def color-adjust
  (lifecycle.composite/describe ColorAdjust
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :hue [:setter prop/scalar :coerce coerce/as-double :default 0
                  :saturation [:setter prop/scalar :coerce coerce/as-double :default 0]
                  :brightness [:setter prop/scalar :coerce coerce/as-double :default 0]
                  :contrast [:setter prop/scalar :coerce coerce/as-double :default 0]]}))

(def color-input
  (lifecycle.composite/describe ColorInput
    :ctor []
    :props {:width [:setter prop/scalar :coerce coerce/as-double :default 0]
            :height [:setter prop/scalar :coerce coerce/as-double :default 0]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :paint [:setter prop/scalar :coerce coerce/paint]}))

(def displacement-map
  (lifecycle.composite/describe DisplacementMap
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :map-data [:setter prop/scalar :coerce coerce/float-map
                       :offset-x [:setter prop/scalar :coerce coerce/as-double :default 0]
                       :offset-y [:setter prop/scalar :coerce coerce/as-double :default 0]
                       :scale-x [:setter prop/scalar :coerce coerce/as-double :default 1]
                       :scale-y [:setter prop/scalar :coerce coerce/as-double :default 1]
                       :wrap [:setter prop/scalar :default false]]}))

(def drop-shadow
  (lifecycle.composite/describe DropShadow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :radius [:setter prop/scalar :coerce coerce/as-double :default 10]
            :width [:setter prop/scalar :coerce coerce/as-double :default 21]
            :height [:setter prop/scalar :coerce coerce/as-double :default 21]
            :blur-type [:setter prop/scalar :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :spread [:setter prop/scalar :coerce coerce/as-double :default 0]
            :color [:setter prop/scalar :coerce coerce/color :default :black]
            :offset-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :offset-y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def gaussian-blur
  (lifecycle.composite/describe GaussianBlur
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :radius [:setter prop/scalar :coerce coerce/as-double :default 10]}))

(def glow
  (lifecycle.composite/describe Glow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :level [:setter prop/scalar :coerce coerce/as-double :default 0.3]}))

(def image-input
  (lifecycle.composite/describe ImageInput
    :ctor []
    :default-prop [:source prop/extract-single]
    :props {:source [:setter prop/scalar :coerce coerce/image]
            :x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def inner-shadow
  (lifecycle.composite/describe InnerShadow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :radius [:setter prop/scalar :coerce coerce/as-double :default 10]
            :width [:setter prop/scalar :coerce coerce/as-double :default 21]
            :height [:setter prop/scalar :coerce coerce/as-double :default 21]
            :blur-type [:setter prop/scalar
                        :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :choke [:setter prop/scalar :coerce coerce/as-double :default 0]
            :color [:setter prop/scalar :coerce coerce/color :default :black]
            :offset-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :offset-y [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def lighting
  (lifecycle.composite/describe Lighting
    :ctor []
    :default-prop [:light prop/extract-single]
    :props {:light [:setter prop/component]
            :bump-input [:setter prop/component]
            :content-input [:setter prop/component]
            :diffuse-constant [:setter prop/scalar :coerce coerce/as-double :default 1]
            :specular-constant [:setter prop/scalar :coerce coerce/as-double :default 0.3]
            :specular-exponent [:setter prop/scalar :coerce coerce/as-double :default 0.3]
            :surface-scale [:setter prop/scalar :coerce coerce/as-double :default 1.5]}))

(def distant-light
  (lifecycle.composite/describe Light$Distant
    :ctor []
    :extends [light]
    :props {:azimuth [:setter prop/scalar :coerce coerce/as-double :default 45]
            :elevation [:setter prop/scalar :coerce coerce/as-double :default 45]}))

(def spot-light
  (lifecycle.composite/describe Light$Spot
    :ctor []
    :extends [point-light]
    :props {:points-at-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :points-at-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :points-at-z [:setter prop/scalar :coerce coerce/as-double :default 0]
            :specular-exponent [:setter prop/scalar :coerce coerce/as-double :default 1]}))

(def motion-blur
  (lifecycle.composite/describe MotionBlur
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :radius [:setter prop/scalar :coerce coerce/as-double :default 10]
            :angle [:setter prop/scalar :coerce coerce/as-double :default 0]}))

(def perspective-transform
  (lifecycle.composite/describe PerspectiveTransform
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :llx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :lly [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :lrx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :lry [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :ulx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :uly [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :urx [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :ury [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def reflection
  (lifecycle.composite/describe Reflection
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :top-offset [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :fraction [:setter prop/scalar :coerce coerce/as-double :default 0.75]
            :top-opacity [:setter prop/scalar :coerce coerce/as-double :default 0.5]
            :bottom-opacity [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def sepia-tone
  (lifecycle.composite/describe SepiaTone
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :level [:setter prop/scalar :coerce coerce/as-double :default 1.0]}))

(def shadow
  (lifecycle.composite/describe Shadow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter prop/component]
            :radius [:setter prop/scalar :coerce coerce/as-double :default 10.0]
            :width [:setter prop/scalar :coerce coerce/as-double :default 21.0]
            :height [:setter prop/scalar :coerce coerce/as-double :default 21.0]
            :blur-type [:setter prop/scalar :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :color [:setter prop/scalar :coerce coerce/color :default :black]}))

(def tag->lifecycle
  {:effect/blend blend
   :effect/bloom bloom
   :effect/box-blur box-blur
   :effect/color-adjust color-adjust
   :effect/color-input color-input
   :effect/displacement-map displacement-map
   :effect/drop-shadow drop-shadow
   :effect/gaussian-blur gaussian-blur
   :effect/glow glow
   :effect/image-input image-input
   :effect/inner-shadow inner-shadow
   :effect/lighting lighting
   :effect/distant-light distant-light
   :effect/point-light point-light
   :effect/spot-light spot-light
   :effect/motion-blur motion-blur
   :effect/perspective-transform perspective-transform
   :effect/reflection reflection
   :effect/sepia-tone sepia-tone
   :effect/shadow shadow})