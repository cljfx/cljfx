(ns cljfx.fx.v-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.layout VBox Priority]
           [javafx.geometry Pos]))

(def lifecycle
  (lifecycle.composite/describe VBox
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:v-box/margin ["vbox-margin" coerce/insets]
                                    :v-box/vgrow ["vbox-vgrow" (coerce/enum Priority)]})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :fill-width [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))