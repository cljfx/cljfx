(ns cljfx.fx.tool-bar
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ToolBar]
           [javafx.geometry Orientation]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (composite/props ToolBar
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "tool-bar"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :tool-bar]
      ;; definitions
      :items [:list lifecycle/dynamics]
      :orientation [:setter lifecycle/scalar
                    :coerce (coerce/enum Orientation)
                    :default :horizontal])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe ToolBar
      :ctor []
      :props props)
    :tool-bar))
