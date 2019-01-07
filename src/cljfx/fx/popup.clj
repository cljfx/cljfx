(ns cljfx.fx.popup
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.popup-window :as fx.popup-window]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.stage Popup]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Popup
    :ctor []
    :extends [fx.popup-window/lifecycle]
    :props {:content [:list lifecycle/dynamics]}))