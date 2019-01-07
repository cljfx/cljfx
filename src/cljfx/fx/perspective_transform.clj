(ns cljfx.fx.perspective-transform
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect PerspectiveTransform]))

(set! *warn-on-reflection* true)

(def lifecycle
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