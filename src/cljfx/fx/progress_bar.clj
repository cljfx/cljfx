(ns cljfx.fx.progress-bar
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.progress-indicator :as fx.progress-indicator]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import (javafx.scene.control ProgressBar)))

(def lifecycle
  (lifecycle.composite/describe ProgressBar
    :ctor []
    :extends [fx.progress-indicator/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "progress-bar"]}))
