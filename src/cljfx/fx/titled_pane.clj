(ns cljfx.fx.titled-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TitledPane]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.labeled/props
    (lifecycle.composite/props TitledPane
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "titled-pane"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :titled-pane]
      ;; definitions
      :animated [:setter lifecycle/scalar :default true]
      :collapsible [:setter lifecycle/scalar :default true]
      :content [:setter lifecycle/dynamic]
      :expanded [:setter lifecycle/scalar :default true])))

(def lifecycle
  (lifecycle.composite/describe TitledPane
    :ctor []
    :props props))
