(ns cljfx.fx.image-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.image ImageView]
           [javafx.scene AccessibleRole]
           [javafx.geometry NodeOrientation]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (lifecycle.composite/props ImageView
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "image-view"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :image-view]
      :node-orientation [:setter lifecycle/scalar :coerce (coerce/enum NodeOrientation)
                         :default :left-to-right]
      ;; definitions
      :image [:setter lifecycle/scalar :coerce coerce/image]
      :x [:setter lifecycle/scalar :coerce double, :default 0
          :y [:setter lifecycle/scalar :coerce double, :default 0]
          :fit-width [:setter lifecycle/scalar :coerce double, :default 0]
          :fit-height [:setter lifecycle/scalar :coerce double, :default 0]
          :preserve-ratio [:setter lifecycle/scalar :default false]
          :smooth [:setter lifecycle/scalar :default ImageView/SMOOTH_DEFAULT]
          :viewport [:setter lifecycle/scalar :coerce coerce/rectangle-2d]])))

(def lifecycle
  (lifecycle.composite/describe ImageView
    :ctor []
    :props props))
