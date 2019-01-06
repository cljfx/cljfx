(ns cljfx.fx.h-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.layout HBox Priority]
           [javafx.geometry Pos]))

(def lifecycle
  (lifecycle.composite/describe HBox
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-extra-props
                                   {:h-box/margin
                                    (prop/make
                                      (mutator/constraint "hbox-margin")
                                      lifecycle/scalar
                                      :coerce coerce/insets)

                                    :h-box/hgrow
                                    (prop/make
                                      (mutator/constraint "hbox-hgrow")
                                      lifecycle/scalar
                                      :coerce (coerce/enum Priority))})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :fill-height [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))