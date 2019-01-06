(ns cljfx.fx.custom-menu-item
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.menu-item :as fx.menu-item])
  (:import [javafx.scene.control CustomMenuItem]))

(def lifecycle
  (lifecycle.composite/describe CustomMenuItem
    :ctor []
    :extends [fx.menu-item/lifecycle]
    :props {:content [:setter lifecycle/dynamic]
            :hide-on-click [:setter lifecycle/scalar :default true]}))