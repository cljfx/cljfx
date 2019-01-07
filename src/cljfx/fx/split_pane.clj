(ns cljfx.fx.split-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control SplitPane]
           [javafx.geometry Orientation]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe SplitPane
    :ctor []
    :extends [fx.control/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default "split-pane"]
            ;; definitions
            :divider-positions [:setter lifecycle/scalar
                                :coerce #(into-array Double/TYPE %)
                                :default []]
            :items [:list lifecycle/dynamics]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]}))