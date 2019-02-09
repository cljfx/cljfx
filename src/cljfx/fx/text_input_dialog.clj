(ns cljfx.fx.text-input-dialog
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
        :props props)
      (lifecycle/wrap-on-delete #(.hide ^TextInputDialog %))))
