(ns cljfx.fx.grid-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.layout GridPane Priority]
           [javafx.geometry HPos VPos Pos]))

(def lifecycle
  (lifecycle.composite/describe GridPane
    :ctor []
    :extends [fx.pane/lifecycle]
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