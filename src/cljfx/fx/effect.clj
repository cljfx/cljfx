(ns cljfx.fx.effect
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
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
    :props {:bottom-input [:setter lifecycle/dynamic]
            :mode [:setter lifecycle/scalar :coerce (coerce/enum BlendMode)]
            :opacity [:setter lifecycle/scalar :coerce double :default 1]
            :top-input [:setter lifecycle/dynamic]}))

(def bloom
  (lifecycle.composite/describe Bloom
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :threshold [:setter lifecycle/scalar :coerce double :default 0.3]}))

(def box-blur
  (lifecycle.composite/describe BoxBlur
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :iterations [:setter lifecycle/scalar :coerce int :default 1]
            :width [:setter lifecycle/scalar :coerce double :default 5]
            :height [:setter lifecycle/scalar :coerce double :default 5]}))

(def color-adjust
  (lifecycle.composite/describe ColorAdjust
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
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
    :props {:input [:setter lifecycle/dynamic]
            :map-data [:setter lifecycle/scalar :coerce coerce/float-map
                       :offset-x [:setter lifecycle/scalar :coerce double :default 0]
                       :offset-y [:setter lifecycle/scalar :coerce double :default 0]
                       :scale-x [:setter lifecycle/scalar :coerce double :default 1]
                       :scale-y [:setter lifecycle/scalar :coerce double :default 1]
                       :wrap [:setter lifecycle/scalar :default false]]}))

(def drop-shadow
  (lifecycle.composite/describe DropShadow
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
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
    :props {:input [:setter lifecycle/dynamic]
            :radius [:setter lifecycle/scalar :coerce double :default 10]}))

(def glow
  (lifecycle.composite/describe Glow
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :level [:setter lifecycle/scalar :coerce double :default 0.3]}))

(def image-input
  (lifecycle.composite/describe ImageInput
    :ctor []
    :props {:source [:setter lifecycle/scalar :coerce coerce/image]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))

(def inner-shadow
  (lifecycle.composite/describe InnerShadow
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
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
    :props {:light [:setter lifecycle/dynamic]
            :bump-input [:setter lifecycle/dynamic]
            :content-input [:setter lifecycle/dynamic]
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
    :props {:input [:setter lifecycle/dynamic]
            :radius [:setter lifecycle/scalar :coerce double :default 10]
            :angle [:setter lifecycle/scalar :coerce double :default 0]}))

(def perspective-transform
  (lifecycle.composite/describe PerspectiveTransform
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
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
    :props {:input [:setter lifecycle/dynamic]
            :top-offset [:setter lifecycle/scalar :coerce double :default 0.0]
            :fraction [:setter lifecycle/scalar :coerce double :default 0.75]
            :top-opacity [:setter lifecycle/scalar :coerce double :default 0.5]
            :bottom-opacity [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def sepia-tone
  (lifecycle.composite/describe SepiaTone
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :level [:setter lifecycle/scalar :coerce double :default 1.0]}))

(def shadow
  (lifecycle.composite/describe Shadow
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :radius [:setter lifecycle/scalar :coerce double :default 10.0]
            :width [:setter lifecycle/scalar :coerce double :default 21.0]
            :height [:setter lifecycle/scalar :coerce double :default 21.0]
            :blur-type [:setter lifecycle/scalar :coerce (coerce/enum BlurType)
                        :default :three-pass-box]
            :color [:setter lifecycle/scalar :coerce coerce/color :default :black]}))

(def keyword->lifecycle
  {:blend blend
   :bloom bloom
   :box-blur box-blur
   :color-adjust color-adjust
   :color-input color-input
   :displacement-map displacement-map
   :drop-shadow drop-shadow
   :gaussian-blur gaussian-blur
   :glow glow
   :image-input image-input
   :inner-shadow inner-shadow
   :lighting lighting
   :distant-light distant-light
   :point-light-effect point-light
   :spot-light spot-light
   :motion-blur motion-blur
   :perspective-transform perspective-transform
   :reflection reflection
   :sepia-tone sepia-tone
   :shadow shadow})