(ns cljfx.fx.split-menu-button
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.menu-button :as fx.menu-button])
  (:import [javafx.scene.control SplitMenuButton]))

(def lifecycle
  (lifecycle.composite/describe SplitMenuButton
    :ctor []
    :extends [fx.menu-button/lifecycle]))