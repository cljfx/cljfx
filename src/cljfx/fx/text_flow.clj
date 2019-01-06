(ns cljfx.fx.text-flow
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.pane :as fx.pane])
  (:import [javafx.scene.text TextFlow TextAlignment]))

(def lifecycle
  (lifecycle.composite/describe TextFlow
    :ctor []
    :extends [fx.pane/lifecycle]
    :props {:line-spacing [:setter lifecycle/scalar :coerce double :default 0.0]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]}))