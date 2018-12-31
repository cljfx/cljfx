(ns cljfx.fx.pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.scene :as fx.scene]
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
    :props {:children [:list lifecycle/dynamics]}))

(def anchor-pane
  (lifecycle.composite/describe AnchorPane
    :ctor []
    :extends [pane]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:anchor-pane/top ["pane-top-anchor" double]
                                    :anchor-pane/left ["pane-left-anchor" double]
                                    :anchor-pane/bottom ["pane-bottom-anchor" double]
                                    :anchor-pane/right ["pane-right-anchor" double]})
                                 lifecycle/wrap-many)]}))

(def ^:private border-pane-dynamic-hiccup
  (lifecycle/wrap-constraints
    lifecycle/dynamic
    {:border-pane/margin ["borderpane-margin" coerce/insets]
     :border-pane/alignment ["borderpane-alignment" (coerce/enum Pos)]}))

(def border-pane
  (lifecycle.composite/describe BorderPane
    :ctor []
    :extends [pane]
    :props {:bottom [:setter border-pane-dynamic-hiccup]
            :center [:setter border-pane-dynamic-hiccup]
            :left [:setter border-pane-dynamic-hiccup]
            :right [:setter border-pane-dynamic-hiccup]
            :top [:setter border-pane-dynamic-hiccup]}))

(def flow-pane
  (lifecycle.composite/describe FlowPane
    :ctor []
    :extends [pane]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:flow-pane/margin ["flowpane-margin" coerce/insets]})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :top-left]
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
    :props {:children
            [:list (-> lifecycle/dynamic
                       (lifecycle/wrap-constraints
                         {:grid-pane/margin ["gridpane-margin" coerce/insets]
                          :grid-pane/halignment ["gridpane-halignment" (coerce/enum HPos)]
                          :grid-pane/valignment ["gridpane-valignment" (coerce/enum VPos)]
                          :grid-pane/hgrow ["gridpane-hgrow" (coerce/enum Priority)]
                          :grid-pane/vgrow ["gridpane-vgrow" (coerce/enum Priority)]
                          :grid-pane/row ["gridpane-row" int]
                          :grid-pane/column ["gridpane-column" int]
                          :grid-pane/row-span ["gridpane-row-span" int]
                          :grid-pane/column-span ["gridpane-column-span" int]
                          :grid-pane/full-width ["gridpane-fill-width" boolean]
                          :grid-pane/full-height ["gridpane-fill-height" boolean]})
                       lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :column-constraints [:list lifecycle/dynamics]
            :grid-lines-visible [:setter lifecycle/scalar :default false]
            :hgap [:setter lifecycle/scalar :coerce double :default 0.0]
            :row-constraints [:list lifecycle/dynamics]
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
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:h-box/margin ["hbox-margin" coerce/insets]
                                    :h-box/hgrow ["hbox-hgrow" (coerce/enum Priority)]})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :fill-height [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def stack-pane
  (lifecycle.composite/describe StackPane
    :ctor []
    :extends [pane]
    :props {:children
            [:list (-> lifecycle/dynamic
                       (lifecycle/wrap-constraints
                         {:stack-pane/alignment ["stackpane-alignment" (coerce/enum Pos)]
                          :stack-pane/margin ["stackpane-margin" coerce/insets]})
                       lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :center]}))

(def text-flow
  (lifecycle.composite/describe TextFlow
    :ctor []
    :extends [pane]
    :props {:line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]}))

(def tile-pane
  (lifecycle.composite/describe TilePane
    :ctor []
    :extends [pane]
    :props {:children
            [:list (-> lifecycle/dynamic
                       (lifecycle/wrap-constraints
                         {:tile-pane/margin ["tilepane-margin" coerce/insets]
                          :tile-pane/alignment ["tilepane-alignment" (coerce/enum Pos)]})
                       lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
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
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:v-box/margin ["vbox-margin" coerce/insets]
                                    :v-box/vgrow ["vbox-vgrow" (coerce/enum Priority)]})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :fill-width [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def keyword->lifecycle
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