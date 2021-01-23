(ns cljfx.fx.text-input-dialog
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.dialog :as fx.dialog]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TextInputDialog]))

(set! *warn-on-reflection* true)

(def props
  fx.dialog/props)

(def lifecycle
  (-> (composite/describe TextInputDialog
        :ctor []
        :props props
        :prop-order {:showing 1})
      (lifecycle/wrap-on-delete #(.hide ^TextInputDialog %))))
