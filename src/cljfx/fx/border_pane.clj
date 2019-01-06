(ns cljfx.fx.border-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.geometry Pos]
           [javafx.scene.layout BorderPane]))

(def ^:private border-pane-constrained-dynamic
  (lifecycle/wrap-constraints
    lifecycle/dynamic
    {:border-pane/margin ["borderpane-margin" coerce/insets]
     :border-pane/alignment ["borderpane-alignment" (coerce/enum Pos)]}))

(def lifecycle
  (lifecycle.composite/describe BorderPane
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:bottom [:setter border-pane-constrained-dynamic]
            :center [:setter border-pane-constrained-dynamic]
            :left [:setter border-pane-constrained-dynamic]
            :right [:setter border-pane-constrained-dynamic]
            :top [:setter border-pane-constrained-dynamic]}))