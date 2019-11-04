(ns cljfx.jdk.fx.tab-pane
  (:require [cljfx.composite :as composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TabPane TabPane$TabDragPolicy TabPane$TabClosingPolicy]))

(set! *warn-on-reflection* true)

(def props
  (composite/props TabPane
    :tab-closing-policy [:setter lifecycle/scalar
                         :coerce (coerce/enum TabPane$TabClosingPolicy)
                         :default :selected-tab]
    :tab-drag-policy [:setter lifecycle/scalar
                      :coerce (coerce/enum TabPane$TabDragPolicy) :default :fixed]))
