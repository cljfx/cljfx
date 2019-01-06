(ns cljfx.fx.stack-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.layout StackPane]
           [javafx.geometry Pos]))

(def lifecycle
  (lifecycle.composite/describe StackPane
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children
            [:list (-> lifecycle/dynamic
                       (lifecycle/wrap-extra-props
                         {:stack-pane/alignment
                          (prop/make
                            (mutator/constraint "stackpane-alignment")
                            lifecycle/scalar
                            :coerce (coerce/enum Pos))

                          :stack-pane/margin
                          (prop/make
                            (mutator/constraint "stackpane-margin")
                            lifecycle/scalar
                            :coerce coerce/insets)})
                       lifecycle/wrap-many)]
            :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                        :default :center]}))