(ns cljfx.fx.check-menu-item
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.menu-item :as fx.menu-item]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control CheckMenuItem]))

(def lifecycle
  (lifecycle.composite/describe CheckMenuItem
    :ctor []
    :extends [fx.menu-item/lifecycle]
    :props {:selected [:setter lifecycle/scalar :default false]}))