(ns cljfx.fx.anchor-pane
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.pane :as fx.pane]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.layout AnchorPane]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.pane/props
    (composite/props AnchorPane
      :children [:list
                 (-> lifecycle/dynamic
                     (lifecycle/wrap-extra-props
                       {:anchor-pane/top (prop/make
                                           (mutator/constraint "pane-top-anchor")
                                           lifecycle/scalar
                                           :coerce double)

                        :anchor-pane/left (prop/make
                                            (mutator/constraint "pane-left-anchor")
                                            lifecycle/scalar
                                            :coerce double)

                        :anchor-pane/bottom (prop/make
                                              (mutator/constraint "pane-bottom-anchor")
                                              lifecycle/scalar
                                              :coerce double)

                        :anchor-pane/right (prop/make
                                             (mutator/constraint "pane-right-anchor")
                                             lifecycle/scalar
                                             :coerce double)})
                     lifecycle/wrap-many)])))

(def lifecycle
  (composite/describe AnchorPane
    :ctor []
    :props props))
