(ns cljfx.fx.image-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.image ImageView]))

(def lifecycle
  (lifecycle.composite/describe ImageView
    :ctor []
    :extends [fx.node/lifecycle]
    :props {:image [:setter lifecycle/scalar :coerce coerce/image]
            :x [:setter lifecycle/scalar :coerce double, :default 0
                :y [:setter lifecycle/scalar :coerce double, :default 0]
                :fit-width [:setter lifecycle/scalar :coerce double, :default 0]
                :fit-height [:setter lifecycle/scalar :coerce double, :default 0]
                :preserve-ratio [:setter lifecycle/scalar :default false]
                :smooth [:setter lifecycle/scalar :default ImageView/SMOOTH_DEFAULT]
                :viewport [:setter lifecycle/scalar :coerce coerce/rectangle-2d]]}))