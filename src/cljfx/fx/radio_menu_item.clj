(ns cljfx.fx.radio-menu-item
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.menu-item :as fx.menu-item])
  (:import [javafx.scene.control RadioMenuItem]))

(def lifecycle
  (lifecycle.composite/describe RadioMenuItem
    :ctor []
    :extends [fx.menu-item/lifecycle]
    :props {:selected [:setter lifecycle/scalar :default false]}))