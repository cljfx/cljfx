(ns cljfx.fx.tab-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control TabPane TabPane$TabClosingPolicy TabPane$TabDragPolicy]
           [javafx.geometry Side]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (lifecycle.composite/props TabPane
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "tab-pane"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tab-pane]
      ;; definitions
      :rotate-graphic [:setter lifecycle/scalar :default false]
      :side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :top]
      :tab-closing-policy [:setter lifecycle/scalar
                           :coerce (coerce/enum TabPane$TabClosingPolicy)
                           :default :selected-tab]
      :tab-drag-policy [:setter lifecycle/scalar
                        :coerce (coerce/enum TabPane$TabDragPolicy) :default :fixed]
      :tab-max-height [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
      :tab-max-width [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
      :tab-min-height [:setter lifecycle/scalar :coerce double :default 0.0]
      :tab-min-width [:setter lifecycle/scalar :coerce double :default 0.0]
      :tabs [:list lifecycle/dynamics])))

(def lifecycle
  (lifecycle.composite/describe TabPane
    :ctor []
    :props props))
