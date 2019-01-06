(ns cljfx.fx.button-bar
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ButtonBar]))

;; TODO wrap constraints

(def lifecycle
  (lifecycle.composite/describe ButtonBar
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:button-min-width [:setter lifecycle/scalar :coerce double]
            :button-order [:setter lifecycle/scalar]
            :buttons [:list lifecycle/dynamics]}))