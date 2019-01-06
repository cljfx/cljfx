(ns cljfx.fx.text-area
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.text-input-control :as fx.text-input-control]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TextArea]))

(def lifecycle
  (lifecycle.composite/describe TextArea
    :ctor []
    :extends [fx.text-input-control/lifecycle]
    :props {:pref-column-count [:setter lifecycle/scalar :coerce int :default 40]
            :pref-row-count [:setter lifecycle/scalar :coerce int :default 10]
            :scroll-left [:setter lifecycle/scalar :coerce double :default 0.0]
            :scroll-top [:setter lifecycle/scalar :coerce double :default 0.0]
            :wrap-text [:setter lifecycle/scalar :default false]}))