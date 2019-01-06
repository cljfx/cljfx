(ns cljfx.fx.flow-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.layout FlowPane]
           [javafx.geometry Pos HPos Orientation VPos]))

(def lifecycle
  (lifecycle.composite/describe FlowPane
    :ctor []
    :extends [fx.pane/lifecycle]
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