(ns cljfx.fx.text-input-dialog
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.dialog :as fx.dialog])
  (:import [javafx.scene.control TextInputDialog]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe TextInputDialog
    :ctor []
    :extends [fx.dialog/lifecycle]))
