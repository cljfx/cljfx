(ns cljfx.fx.stack-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.layout StackPane]
           [javafx.geometry Pos]))

(def lifecycle
  (lifecycle.composite/describe StackPane
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children
            [:list (-> lifecycle/dynamic
                       (lifecycle/wrap-constraints
                         {:stack-pane/alignment ["stackpane-alignment" (coerce/enum Pos)]
                          :stack-pane/margin ["stackpane-margin" coerce/insets]})
                       lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :center]}))