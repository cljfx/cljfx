(ns cljfx.fx.button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.button-base :as fx.button-base]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control Button]))

(def lifecycle
  (lifecycle.composite/describe Button
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {:cancel-button [:setter lifecycle/scalar :default false]
            :default-button [:setter lifecycle/scalar :default false]}))