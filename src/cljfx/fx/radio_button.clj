(ns cljfx.fx.radio-button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.toggle-button :as fx.toggle-button])
  (:import [javafx.scene.control RadioButton]))

(def lifecycle
  (lifecycle.composite/describe RadioButton
    :ctor []
    :extends [fx.toggle-button/lifecycle]))