(ns cljfx.fx.flow-pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.layout FlowPane]
           [javafx.geometry Pos HPos Orientation VPos]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    (composite/props FlowPane
      :children [:list (-> lifecycle/dynamic
                           (lifecycle/wrap-extra-props
                             {:flow-pane/margin (prop/make
                                                  (mutator/constraint "flowpane-margin")
                                                  lifecycle/scalar
                                                  :coerce coerce/insets)})
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
      :vgap [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (composite/describe FlowPane
    :ctor []
    :props props))
