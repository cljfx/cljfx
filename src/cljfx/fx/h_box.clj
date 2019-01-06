(ns cljfx.fx.h-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.layout HBox Priority]
           [javafx.geometry Pos]))

(def lifecycle
  (lifecycle.composite/describe HBox
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:h-box/margin ["hbox-margin" coerce/insets]
                                    :h-box/hgrow ["hbox-hgrow" (coerce/enum Priority)]})
                                 lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :top-left]
            :fill-height [:setter lifecycle/scalar :default true]
            :spacing [:setter lifecycle/scalar :coerce double :default 0.0]}))