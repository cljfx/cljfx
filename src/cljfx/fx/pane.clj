(ns cljfx.fx.pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce])
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
    :props {:children [:list prop/component-vec]}))

(def anchor-pane
  (lifecycle.composite/describe AnchorPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]))

(def border-pane
  (lifecycle.composite/describe BorderPane
    :ctor []
    :extends [pane]
    :props {:bottom [:setter prop/component]
            :center [:setter prop/component]
            :left [:setter prop/component]
            :right [:setter prop/component]
            :top [:setter prop/component]}))

(def flow-pane
  (lifecycle.composite/describe FlowPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :top-left]
            :column-halignment [:setter prop/scalar :coerce (coerce/enum HPos)
                                :default :left]
            :hgap [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :pref-wrap-length [:setter prop/scalar :coerce coerce/as-double :default 400.0]
            :row-valignment [:setter prop/scalar :coerce (coerce/enum VPos)
                             :default :center]
            :vgap [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def grid-pane
  (lifecycle.composite/describe GridPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :top-left]
            :column-constraints [:list prop/component-vec]
            :grid-lines-visible [:setter prop/scalar :default false]
            :hgap [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :row-constraints [:list prop/component-vec]
            :vgap [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def column-constraints
  (lifecycle.composite/describe ColumnConstraints
    :ctor []
    :props {:fill-width [:setter prop/scalar :default true]
            :halignment [:setter prop/scalar :coerce (coerce/enum HPos)]
            :hgrow [:setter prop/scalar :coerce (coerce/enum Priority)]
            :max-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :min-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :percent-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :pref-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]}))

(def row-constraints
  (lifecycle.composite/describe RowConstraints
    :ctor []
    :props {:fill-height [:setter prop/scalar :default true]
            :max-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :min-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :percent-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :pref-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :valignment [:setter prop/scalar :coerce (coerce/enum VPos)]
            :vgrow [:setter prop/scalar :coerce (coerce/enum Priority)]}))

(def h-box
  (lifecycle.composite/describe HBox
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :top-left]
            :fill-height [:setter prop/scalar :default true]
            :spacing [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def stack-pane
  (lifecycle.composite/describe StackPane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :center]}))

(def text-flow
  (lifecycle.composite/describe TextFlow
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:line-spacing [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :text-alignment [:setter prop/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]}))

(def tile-pane
  (lifecycle.composite/describe TilePane
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :top-left]
            :hgap [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :pref-columns [:setter prop/scalar :coerce coerce/as-int :default 5]
            :pref-rows [:setter prop/scalar :coerce coerce/as-int :default 5]
            :pref-tile-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :pref-tile-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :tile-alignment [:setter prop/scalar :coerce (coerce/enum Pos)
                             :default :center]
            :vgap [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

(def v-box
  (lifecycle.composite/describe VBox
    :ctor []
    :extends [pane]
    :default-prop [:children prop/extract-all]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :top-left]
            :fill-width [:setter prop/scalar :default true]
            :spacing [:setter prop/scalar :coerce coerce/as-double :default 0.0]}))

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