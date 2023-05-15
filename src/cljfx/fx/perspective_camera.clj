(ns cljfx.fx.perspective-camera
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.camera :as fx.camera]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene PerspectiveCamera]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.camera/props
    (composite/props PerspectiveCamera
      :field-of-view [:setter lifecycle/scalar :coerce double :default 30.0]
      :vertical-field-of-view [:setter lifecycle/scalar :default true]
      :fixed-eye-at-camera-zero [mutator/forbidden lifecycle/scalar])))

(def lifecycle
  (lifecycle/annotate
    (composite/lifecycle
      {:props props
       :args [:fixed-eye-at-camera-zero]
       :ctor (fn [fixed-eye-at-camera-zero]
               (PerspectiveCamera. (boolean fixed-eye-at-camera-zero)))})
    :perspective-camera))
