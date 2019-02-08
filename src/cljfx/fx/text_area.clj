(ns cljfx.fx.text-area
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.text-input-control :as fx.text-input-control]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TextArea]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.text-input-control/props
    (lifecycle.composite/props TextArea
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["text-input" "text-area"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :text-area]
      ;; definitions
      :pref-column-count [:setter lifecycle/scalar :coerce int :default 40]
      :pref-row-count [:setter lifecycle/scalar :coerce int :default 10]
      :scroll-left [:setter lifecycle/scalar :coerce double :default 0.0]
      :scroll-top [:setter lifecycle/scalar :coerce double :default 0.0]
      :wrap-text [:setter lifecycle/scalar :default false])))

(def lifecycle
  (lifecycle.composite/describe TextArea
    :ctor []
    :props props))
