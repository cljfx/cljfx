(ns cljfx.fx.html-editor
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.control :as fx.control]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.web HTMLEditor]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (lifecycle.composite/props HTMLEditor
      :html-text [:setter lifecycle/scalar :default
                  "<html><head></head><body contenteditable=\"true\"></body></html>"])))


(def lifecycle
  (lifecycle.composite/describe HTMLEditor
    :ctor []
    :props props))
