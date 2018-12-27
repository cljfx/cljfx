(ns cljfx.fx.pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.layout Pane AnchorPane BorderPane FlowPane GridPane
                                ColumnConstraints Priority RowConstraints HBox StackPane
                                TilePane VBox]
           [javafx.geometry HPos Pos Orientation VPos]
           [javafx.scene.text TextFlow TextAlignment]))

(set! *warn-on-reflection* true)

(def pane
  (lifecycle.composite/describe Pane
    :ctor []
    :extends [fx.scene/region]
    :default-prop [:children prop/extract-all]
    :props {:children [:list lifecycle/many-dynamic-hiccups]}))

(def anchor-pane
  (lifecycle.composite/describe AnchorPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]))

(def border-pane
  (lifecycle.composite/describe BorderPane
    :ctor []
    :extends [pane]
    :props {:bottom [:setter lifecycle/dynamic-hiccup]
            :center [:setter lifecycle/dynamic-hiccup]
            :left [:setter lifecycle/dynamic-hiccup]
            :right [:setter lifecycle/dynamic-hiccup]
            :top [:setter lifecycle/dynamic-hiccup]}))

(def flow-pane
  (lifecycle.composite/describe FlowPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :top-left]
            :column-halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos)
                                :default :left]
            :hgap [:setter lifecycle/scalar :coerce double :default 0.0]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :pref-wrap-length [:setter lifecycle/scalar :coerce double :default 400.0]
            :row-valignment [:setter lifecycle/scalar :coerce (coerce/enum VPos)
                             :default :center]
            :vgap [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def grid-pane
  (lifecycle.composite/describe GridPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :top-left]
            :column-constraints [:list lifecycle/many-dynamic-hiccups]
            :grid-lines-visible [:setter lifecycle/scalar :default false]
            :hgap [:setter lifecycle/scalar :coerce double :default 0.0]
            :row-constraints [:list lifecycle/many-dynamic-hiccups]
            :vgap [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def column-constraints
  (lifecycle.composite/describe ColumnConstraints
    :ctor []
    :props {:fill-width [:setter lifecycle/scalar :default true]
            :halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos)]
            :hgrow [:setter lifecycle/scalar :coerce (coerce/enum Priority)]
            :max-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :percent-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default -1.0]}))

(def row-constraints
  (lifecycle.composite/describe RowConstraints
    :ctor []
    :props {:fill-height [:setter lifecycle/scalar :default true]
            :max-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :percent-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :valignment [:setter lifecycle/scalar :coerce (coerce/enum VPos)]
            :vgrow [:setter lifecycle/scalar :coerce (coerce/enum Priority)]}))

(def h-box
  (lifecycle.composite/describe HBox
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :top-left]
            :fill-height [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def stack-pane
  (lifecycle.composite/describe StackPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :center]}))

(def text-flow
  (lifecycle.composite/describe TextFlow
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]}))

(def tile-pane
  (lifecycle.composite/describe TilePane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :top-left]
            :hgap [:setter lifecycle/scalar :coerce double :default 0.0]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :pref-columns [:setter lifecycle/scalar :coerce int :default 5]
            :pref-rows [:setter lifecycle/scalar :coerce int :default 5]
            :pref-tile-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-tile-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :tile-alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                             :default :center]
            :vgap [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def v-box
  (lifecycle.composite/describe VBox
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :top-left]
            :fill-width [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def tag->lifecycle
  {:pane pane
   :anchor-pane anchor-pane
   :border-pane border-pane
   :flow-pane flow-pane
   :grid-pane grid-pane
   :row-constraints row-constraints
   :column-constraints column-constraints
   :h-box h-box
   :stack-pane stack-pane
   :text-flow text-flow
   :tile-pane tile-pane
   :v-box v-box})