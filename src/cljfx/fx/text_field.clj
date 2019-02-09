(ns cljfx.fx.text-field
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.text-input-control :as fx.text-input-control])
  (:import [javafx.scene.control TextField]
           [javafx.geometry Pos]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.text-input-control/props
    (composite/props TextField
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["text-input" "text-field"]]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :text-field]
      ;; definitions
      :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :center-left]
      :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :pref-column-count [:setter lifecycle/scalar :coerce int :default 12])))

(def lifecycle
  (composite/describe TextField
    :ctor []
    :props props))
