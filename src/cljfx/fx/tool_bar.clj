(ns cljfx.fx.tool-bar
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ToolBar]
           [javafx.geometry Orientation]))

(def lifecycle
  (lifecycle.composite/describe ToolBar
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:items [:list lifecycle/dynamics]
            :orientation [:setter lifecycle/scalar
                          :coerce (coerce/enum Orientation)
                          :default :horizontal]}))