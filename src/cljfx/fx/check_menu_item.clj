(ns cljfx.fx.check-menu-item
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.menu-item :as fx.menu-item]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control CheckMenuItem]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe CheckMenuItem
    :ctor []
    :extends [fx.menu-item/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["check-menu-item" "menu-item"]]
            ;; definitions
            :selected [:setter lifecycle/scalar :default false]}))