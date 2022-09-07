(ns cljfx.fx.h-box
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.layout HBox Priority]
           [javafx.geometry Pos]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    (composite/props HBox
      :children [:list (-> lifecycle/dynamic
                           (lifecycle/wrap-extra-props
                             {:h-box/margin (prop/make
                                              (mutator/constraint "hbox-margin")
                                              lifecycle/scalar
                                              :coerce coerce/insets)
                              :h-box/hgrow (prop/make
                                             (mutator/constraint "hbox-hgrow")
                                             lifecycle/scalar
                                             :coerce (coerce/enum Priority))})
                           lifecycle/wrap-many)]
      :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                  :default :top-left]
      :fill-height [:setter lifecycle/scalar :default true]
      :spacing [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe HBox
      :ctor []
      :props props)
    :h-box))
