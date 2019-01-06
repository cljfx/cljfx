(ns cljfx.fx.button-bar
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control ButtonBar ButtonBar$ButtonData]))

(def lifecycle
  (lifecycle.composite/describe ButtonBar
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:button-min-width [:setter lifecycle/scalar :coerce double]
            :button-order [:setter lifecycle/scalar]
            :buttons [:list (-> lifecycle/dynamic
                                (lifecycle/wrap-extra-props
                                  {:button-bar/button-data
                                   (prop/make
                                     (mutator/setter #(ButtonBar/setButtonData %1 %2))
                                     lifecycle/scalar
                                     :coerce (coerce/enum ButtonBar$ButtonData))})
                                lifecycle/wrap-many)]}))