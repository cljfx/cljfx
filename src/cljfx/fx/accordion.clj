(ns cljfx.fx.accordion
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Accordion]))

(def lifecycle
  (lifecycle.composite/describe Accordion
    :ctor []
    :extends [fx.control/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "accordion"]
            ;; definitions
            :panes [:list lifecycle/dynamics]}))