(ns cljfx.fx.grid-pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.layout GridPane Priority]
           [javafx.geometry HPos VPos Pos]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    (composite/props GridPane
      :children [:list
                 (-> lifecycle/dynamic
                     (lifecycle/wrap-extra-props
                       {:grid-pane/margin
                        (prop/make
                          (mutator/constraint "gridpane-margin")
                          lifecycle/scalar
                          :coerce coerce/insets)

                        :grid-pane/halignment
                        (prop/make
                          (mutator/constraint "gridpane-halignment")
                          lifecycle/scalar
                          :coerce (coerce/enum HPos))

                        :grid-pane/valignment
                        (prop/make
                          (mutator/constraint "gridpane-valignment")
                          lifecycle/scalar
                          :coerce (coerce/enum VPos))

                        :grid-pane/hgrow
                        (prop/make
                          (mutator/constraint "gridpane-hgrow")
                          lifecycle/scalar
                          :coerce (coerce/enum Priority))

                        :grid-pane/vgrow
                        (prop/make
                          (mutator/constraint "gridpane-vgrow")
                          lifecycle/scalar
                          :coerce (coerce/enum Priority))

                        :grid-pane/row
                        (prop/make
                          (mutator/constraint "gridpane-row")
                          lifecycle/scalar
                          :coerce int)

                        :grid-pane/column
                        (prop/make
                          (mutator/constraint "gridpane-column")
                          lifecycle/scalar
                          :coerce int)

                        :grid-pane/row-span
                        (prop/make
                          (mutator/constraint "gridpane-row-span")
                          lifecycle/scalar
                          :coerce int)

                        :grid-pane/column-span
                        (prop/make
                          (mutator/constraint "gridpane-column-span")
                          lifecycle/scalar
                          :coerce int)

                        :grid-pane/fill-width
                        (prop/make
                          (mutator/constraint "gridpane-fill-width")
                          lifecycle/scalar
                          :coerce boolean)

                        :grid-pane/fill-height
                        (prop/make
                          (mutator/constraint "gridpane-fill-height")
                          lifecycle/scalar
                          :coerce boolean)})
                     lifecycle/wrap-many)]
      :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                  :default :top-left]
      :column-constraints [:list lifecycle/dynamics]
      :grid-lines-visible [:setter lifecycle/scalar :default false]
      :hgap [:setter lifecycle/scalar :coerce double :default 0.0]
      :row-constraints [:list lifecycle/dynamics]
      :vgap [:setter lifecycle/scalar :coerce double :default 0.0])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe GridPane
      :ctor []
      :props props
      :prop-order {:grid-lines-visible 1})
    :grid-pane))
