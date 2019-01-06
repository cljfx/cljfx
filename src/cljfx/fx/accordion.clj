(ns cljfx.fx.accordion
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Accordion]))

(def lifecycle
  (lifecycle.composite/describe Accordion
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:panes [:list lifecycle/dynamics]}))