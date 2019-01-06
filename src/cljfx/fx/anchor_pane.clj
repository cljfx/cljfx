(ns cljfx.fx.anchor-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.layout AnchorPane]))

(def lifecycle
  (lifecycle.composite/describe AnchorPane
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-extra-props
                                   {:anchor-pane/top
                                    (prop/make
                                      (mutator/constraint "pane-top-anchor")
                                      lifecycle/scalar
                                      :coerce double)

                                    :anchor-pane/left
                                    (prop/make
                                      (mutator/constraint "pane-left-anchor")
                                      lifecycle/scalar
                                      :coerce double)

                                    :anchor-pane/bottom
                                    (prop/make
                                      (mutator/constraint "pane-bottom-anchor")
                                      lifecycle/scalar
                                      :coerce double)

                                    :anchor-pane/right
                                    (prop/make
                                      (mutator/constraint "pane-right-anchor")
                                      lifecycle/scalar
                                      :coerce double)})
                                 lifecycle/wrap-many)]}))