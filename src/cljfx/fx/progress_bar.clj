(ns cljfx.fx.progress-bar
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.progress-indicator :as fx.progress-indicator]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import (javafx.scene.control ProgressBar)))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.progress-indicator/props
    (composite/props ProgressBar
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "progress-bar"])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe ProgressBar
      :ctor []
      :props props)
    :progress-bar))
