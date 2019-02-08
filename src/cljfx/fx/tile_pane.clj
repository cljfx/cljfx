(ns cljfx.fx.tile-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.layout TilePane]
           [javafx.geometry Pos Orientation]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    (lifecycle.composite/props TilePane
      :children [:list (-> lifecycle/dynamic
                           (lifecycle/wrap-extra-props
                             {:tile-pane/margin
                              (prop/make
                                (mutator/constraint "tilepane-margin")
                                lifecycle/scalar
                                :coerce coerce/insets)

                              :tile-pane/alignment
                              (prop/make
                                (mutator/constraint "tilepane-alignment")
                                lifecycle/scalar
                                :coerce (coerce/enum Pos))})
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
      :vgap [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle.composite/describe TilePane
    :ctor []
    :props props))
