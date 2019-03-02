(ns cljfx.fx.border-pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.geometry Pos]
           [javafx.scene.layout BorderPane]))

(set! *warn-on-reflection* true)

(def ^:private border-pane-constrained-dynamic
  (lifecycle/wrap-extra-props
    lifecycle/dynamic
    {:border-pane/margin (prop/make
                           (mutator/constraint "borderpane-margin")
                           lifecycle/scalar
                           :coerce coerce/insets)
     :border-pane/alignment (prop/make
                              (mutator/constraint "borderpane-alignment")
                              lifecycle/scalar
                              :coerce (coerce/enum Pos))}))

(def props
  (merge
    fx.pane/props
    (composite/props BorderPane
      :bottom [:setter border-pane-constrained-dynamic]
      :center [:setter border-pane-constrained-dynamic]
      :left [:setter border-pane-constrained-dynamic]
      :right [:setter border-pane-constrained-dynamic]
      :top [:setter border-pane-constrained-dynamic])))

(def lifecycle
  (composite/describe BorderPane
    :ctor []
    :props props))
