(ns cljfx.fx.progress-indicator
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control ProgressIndicator]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (lifecycle.composite/props ProgressIndicator
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "progress-indicator"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :progress-indicator]
      ;; definitions
      :progress [:setter lifecycle/scalar :coerce double :default -1.0])))

(def lifecycle
  (lifecycle.composite/describe ProgressIndicator
    :ctor []
    :props props))
