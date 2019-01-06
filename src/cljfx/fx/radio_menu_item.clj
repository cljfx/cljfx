(ns cljfx.fx.radio-menu-item
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.menu-item :as fx.menu-item]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control RadioMenuItem]))

(def lifecycle
  (lifecycle.composite/describe RadioMenuItem
    :ctor []
    :extends [fx.menu-item/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["radio-menu-item" "menu-item"]]
            ;; definitions
            :selected [:setter lifecycle/scalar :default false]}))