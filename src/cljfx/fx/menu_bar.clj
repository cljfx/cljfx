(ns cljfx.fx.menu-bar
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control MenuBar]))

(def lifecycle
  (lifecycle.composite/describe MenuBar
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:menus [:list lifecycle/dynamics]
            :use-system-menu-bar [:setter lifecycle/scalar :default false]}))