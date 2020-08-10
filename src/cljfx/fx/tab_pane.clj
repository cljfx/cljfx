(ns cljfx.fx.tab-pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control]
            [cljfx.prop :as prop]
            [cljfx.jdk.fx.tab-pane :as jdk.fx.tab-pane]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.control TabPane]
           [javafx.geometry Side]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    jdk.fx.tab-pane/props
    (composite/props TabPane
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "tab-pane"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tab-pane]
      ;; definitions
      :rotate-graphic [:setter lifecycle/scalar :default false]
      :side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :top]
      :tab-max-height [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
      :tab-max-width [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
      :tab-min-height [:setter lifecycle/scalar :coerce double :default 0.0]
      :tab-min-width [:setter lifecycle/scalar :coerce double :default 0.0]
      :tabs [:list lifecycle/dynamics]
      :on-tabs-changed (prop/make (mutator/list-change-listener #(.getTabs ^TabPane %))
                                  lifecycle/list-change-listener))))

(def lifecycle
  (composite/describe TabPane
    :ctor []
    :props props))
