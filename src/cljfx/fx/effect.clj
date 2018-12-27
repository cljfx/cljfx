(ns cljfx.fx.effect
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect Blend BlendMode Bloom BoxBlur ColorAdjust ColorInput
                                DisplacementMap DropShadow BlurType Light$Spot Light$Point
                                Light$Distant MotionBlur PerspectiveTransform Reflection
                                SepiaTone Shadow GaussianBlur Glow ImageInput InnerShadow
                                Lighting Light]))

(set! *warn-on-reflection* true)

(def light
  (lifecycle.composite/describe Light
    :props {:color [:setter lifecycle/scalar :coerce coerce/color :default :white]}))

(def point-light
  (lifecycle.composite/describe Light$Point
    :ctor []
    :extends [light]
    :props {:x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]
            :z [:setter lifecycle/scalar :coerce double :default 0]}))

(def blend
  (lifecycle.composite/describe Blend
    :ctor []
    :props {:bottom-input [:setter lifecycle/dynamic-hiccup]
            :mode [:setter lifecycle/scalar :coerce (coerce/enum BlendMode)]
            :opacity [:setter lifecycle/scalar :coerce double :default 1]
            :top-input [:setter lifecycle/dynamic-hiccup]}))

(def bloom
  (lifecycle.composite/describe Bloom
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :threshold [:setter lifecycle/scalar :coerce double :default 0.3]}))

(def box-blur
  (lifecycle.composite/describe BoxBlur
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :iterations [:setter lifecycle/scalar :coerce int :default 1]
            :width [:setter lifecycle/scalar :coerce double :default 5]
            :height [:setter lifecycle/scalar :coerce double :default 5]}))

(def color-adjust
  (lifecycle.composite/describe ColorAdjust
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :hue [:setter lifecycle/scalar :coerce double :default 0
                  :saturation [:setter lifecycle/scalar :coerce double :default 0]
                  :brightness [:setter lifecycle/scalar :coerce double :default 0]
                  :contrast [:setter lifecycle/scalar :coerce double :default 0]]}))

(def color-input
  (lifecycle.composite/describe ColorInput
    :ctor []
    :props {:width [:setter lifecycle/scalar :coerce double :default 0]
            :height [:setter lifecycle/scalar :coerce double :default 0]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]
            :paint [:setter lifecycle/scalar :coerce coerce/paint]}))

(def displacement-map
  (lifecycle.composite/describe DisplacementMap
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :map-data [:setter lifecycle/scalar :coerce coerce/float-map
                       :offset-x [:setter lifecycle/scalar :coerce double :default 0]
                       :offset-y [:setter lifecycle/scalar :coerce double :default 0]
                       :scale-x [:setter lifecycle/scalar :coerce double :default 1]
                       :scale-y [:setter lifecycle/scalar :coerce double :default 1]
                       :wrap [:setter lifecycle/scalar :default false]]}))

(def drop-shadow
  (lifecycle.composite/describe DropShadow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :radius [:setter lifecycle/scalar :coerce double :default 10]
            :width [:setter lifecycle/scalar :coerce double :default 21]
            :height [:setter lifecycle/scalar :coerce double :default 21]
            :blur-type [:setter lifecycle/scalar :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :spread [:setter lifecycle/scalar :coerce double :default 0]
            :color [:setter lifecycle/scalar :coerce coerce/color :default :black]
            :offset-x [:setter lifecycle/scalar :coerce double :default 0]
            :offset-y [:setter lifecycle/scalar :coerce double :default 0]}))

(def gaussian-blur
  (lifecycle.composite/describe GaussianBlur
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :radius [:setter lifecycle/scalar :coerce double :default 10]}))

(def glow
  (lifecycle.composite/describe Glow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :level [:setter lifecycle/scalar :coerce double :default 0.3]}))

(def image-input
  (lifecycle.composite/describe ImageInput
    :ctor []
    :default-prop [:source prop/extract-single]
    :props {:source [:setter lifecycle/scalar :coerce coerce/image]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def inner-shadow
  (lifecycle.composite/describe InnerShadow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :radius [:setter lifecycle/scalar :coerce double :default 10]
            :width [:setter lifecycle/scalar :coerce double :default 21]
            :height [:setter lifecycle/scalar :coerce double :default 21]
            :blur-type [:setter lifecycle/scalar
                        :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :choke [:setter lifecycle/scalar :coerce double :default 0]
            :color [:setter lifecycle/scalar :coerce coerce/color :default :black]
            :offset-x [:setter lifecycle/scalar :coerce double :default 0]
            :offset-y [:setter lifecycle/scalar :coerce double :default 0]}))

(def lighting
  (lifecycle.composite/describe Lighting
    :ctor []
    :default-prop [:light prop/extract-single]
    :props {:light [:setter lifecycle/dynamic-hiccup]
            :bump-input [:setter lifecycle/dynamic-hiccup]
            :content-input [:setter lifecycle/dynamic-hiccup]
            :diffuse-constant [:setter lifecycle/scalar :coerce double :default 1]
            :specular-constant [:setter lifecycle/scalar :coerce double :default 0.3]
            :specular-exponent [:setter lifecycle/scalar :coerce double :default 0.3]
            :surface-scale [:setter lifecycle/scalar :coerce double :default 1.5]}))

(def distant-light
  (lifecycle.composite/describe Light$Distant
    :ctor []
    :extends [light]
    :props {:azimuth [:setter lifecycle/scalar :coerce double :default 45]
            :elevation [:setter lifecycle/scalar :coerce double :default 45]}))

(def spot-light
  (lifecycle.composite/describe Light$Spot
    :ctor []
    :extends [point-light]
    :props {:points-at-x [:setter lifecycle/scalar :coerce double :default 0]
            :points-at-y [:setter lifecycle/scalar :coerce double :default 0]
            :points-at-z [:setter lifecycle/scalar :coerce double :default 0]
            :specular-exponent [:setter lifecycle/scalar :coerce double :default 1]}))

(def motion-blur
  (lifecycle.composite/describe MotionBlur
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :radius [:setter lifecycle/scalar :coerce double :default 10]
            :angle [:setter lifecycle/scalar :coerce double :default 0]}))

(def perspective-transform
  (lifecycle.composite/describe PerspectiveTransform
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :llx [:setter lifecycle/scalar :coerce double :default 0.0]
            :lly [:setter lifecycle/scalar :coerce double :default 0.0]
            :lrx [:setter lifecycle/scalar :coerce double :default 0.0]
            :lry [:setter lifecycle/scalar :coerce double :default 0.0]
            :ulx [:setter lifecycle/scalar :coerce double :default 0.0]
            :uly [:setter lifecycle/scalar :coerce double :default 0.0]
            :urx [:setter lifecycle/scalar :coerce double :default 0.0]
            :ury [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def reflection
  (lifecycle.composite/describe Reflection
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :top-offset [:setter lifecycle/scalar :coerce double :default 0.0]
            :fraction [:setter lifecycle/scalar :coerce double :default 0.75]
            :top-opacity [:setter lifecycle/scalar :coerce double :default 0.5]
            :bottom-opacity [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def sepia-tone
  (lifecycle.composite/describe SepiaTone
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :level [:setter lifecycle/scalar :coerce double :default 1.0]}))

(def shadow
  (lifecycle.composite/describe Shadow
    :ctor []
    :default-prop [:input prop/extract-single]
    :props {:input [:setter lifecycle/dynamic-hiccup]
            :radius [:setter lifecycle/scalar :coerce double :default 10.0]
            :width [:setter lifecycle/scalar :coerce double :default 21.0]
            :height [:setter lifecycle/scalar :coerce double :default 21.0]
            :blur-type [:setter lifecycle/scalar :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :color [:setter lifecycle/scalar :coerce coerce/color :default :black]}))

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