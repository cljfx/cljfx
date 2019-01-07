(ns cljfx.fx.v-line-to
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.path-element :as fx.path-element])
  (:import [javafx.scene.shape VLineTo]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe VLineTo
    :ctor []
    :extends [fx.path-element/lifecycle]
    :props {:y [:setter lifecycle/scalar :coerce double :default 0]}))