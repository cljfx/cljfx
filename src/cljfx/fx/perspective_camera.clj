(ns cljfx.fx.perspective-camera
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.camera :as fx.camera])
  (:import [javafx.scene PerspectiveCamera]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe PerspectiveCamera
    :ctor []
    :extends [fx.camera/lifecycle]
    :props {:field-of-view [:setter lifecycle/scalar :coerce double :default 30.0]
            :vertical-field-of-view [:setter lifecycle/scalar :default true]}))