(ns cljfx.fx.color-input
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect ColorInput]))

(def lifecycle
  (lifecycle.composite/describe ColorInput
    :ctor []
    :props {:width [:setter lifecycle/scalar :coerce double :default 0]
            :height [:setter lifecycle/scalar :coerce double :default 0]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]
            :paint [:setter lifecycle/scalar :coerce coerce/paint]}))