(ns cljfx.fx.html-editor
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.control :as fx.control]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.web HTMLEditor]))

(def lifecycle
  (lifecycle.composite/describe HTMLEditor
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:html-text
            [:setter lifecycle/scalar :default
             "<html><head></head><body contenteditable=\"true\"></body></html>"]}))