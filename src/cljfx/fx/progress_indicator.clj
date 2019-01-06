(ns cljfx.fx.progress-indicator
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ProgressIndicator]))

(def lifecycle
  (lifecycle.composite/describe ProgressIndicator
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:progress [:setter lifecycle/scalar :coerce double :default -1.0]}))