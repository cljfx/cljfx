(ns cljfx.fx.perspective-transform
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.effect PerspectiveTransform]))

(set! *warn-on-reflection* true)

(def props
  (composite/props PerspectiveTransform
    :input [:setter lifecycle/dynamic]
    :llx [:setter lifecycle/scalar :coerce double :default 0.0]
    :lly [:setter lifecycle/scalar :coerce double :default 0.0]
    :lrx [:setter lifecycle/scalar :coerce double :default 0.0]
    :lry [:setter lifecycle/scalar :coerce double :default 0.0]
    :ulx [:setter lifecycle/scalar :coerce double :default 0.0]
    :uly [:setter lifecycle/scalar :coerce double :default 0.0]
    :urx [:setter lifecycle/scalar :coerce double :default 0.0]
    :ury [:setter lifecycle/scalar :coerce double :default 0.0]))

(def lifecycle
  (composite/describe PerspectiveTransform
    :ctor []
    :props props))
