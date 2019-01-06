(ns cljfx.fx.anchor-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.layout AnchorPane]))

(def lifecycle
  (lifecycle.composite/describe AnchorPane
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:children [:list (-> lifecycle/dynamic
                                 (lifecycle/wrap-constraints
                                   {:anchor-pane/top ["pane-top-anchor" double]
                                    :anchor-pane/left ["pane-left-anchor" double]
                                    :anchor-pane/bottom ["pane-bottom-anchor" double]
                                    :anchor-pane/right ["pane-right-anchor" double]})
                                 lifecycle/wrap-many)]}))