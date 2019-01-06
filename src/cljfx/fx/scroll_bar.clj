(ns cljfx.fx.scroll-bar
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ScrollBar]
           [javafx.geometry Orientation]))

(def lifecycle
  (lifecycle.composite/describe ScrollBar
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:block-increment [:setter lifecycle/scalar :coerce double :default 10.0]
            :max [:setter lifecycle/scalar :coerce double :default 100.0]
            :min [:setter lifecycle/scalar :coerce double :default 0.0]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation) :default :horizontal]
            :unit-increment [:setter lifecycle/scalar :coerce double :default 1.0]
            :value [:setter lifecycle/scalar :coerce double :default 0.0]
            :visible-amount [:setter lifecycle/scalar :coerce double :default 15.0]}))