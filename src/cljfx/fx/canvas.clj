(ns cljfx.fx.canvas
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.fx.node :as fx.node]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.canvas Canvas]
           [javafx.geometry NodeOrientation]))

(def lifecycle
  (lifecycle.composite/describe Canvas
    :ctor []
    :prop-order {:draw 1}
    :extends [fx.node/lifecycle]
    :props {;; overrides
            :node-orientation [:setter lifecycle/scalar :coerce (coerce/enum NodeOrientation)
                               :default :left-to-right]
            ;; definitions
            :height [:setter lifecycle/scalar :coerce double :default 0.0]
            :width [:setter lifecycle/scalar :coerce double :default 0.0]
            :draw [(mutator/setter #(%2 %1))
                   lifecycle/scalar
                   :default
                   (fn [^Canvas canvas]
                     (.clearRect
                       (.getGraphicsContext2D canvas)
                       0
                       0
                       (.getWidth canvas)
                       (.getHeight canvas)))]}))