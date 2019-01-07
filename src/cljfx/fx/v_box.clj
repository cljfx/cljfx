(ns cljfx.fx.v-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.layout VBox Priority]
           [javafx.geometry Pos]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe VBox
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-extra-props
                                   {:v-box/margin
                                    (prop/make
                                      (mutator/constraint "vbox-margin")
                                      lifecycle/scalar
                                      :coerce coerce/insets)

                                    :v-box/vgrow
                                    (prop/make
                                      (mutator/constraint "vbox-vgrow")
                                      lifecycle/scalar
                                      :coerce (coerce/enum Priority))})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :fill-width [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))