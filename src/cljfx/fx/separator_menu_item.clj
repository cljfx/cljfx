(ns cljfx.fx.separator-menu-item
  "Part of a public API"
  (:require [cljfx.coerce :as coerce]
            [cljfx.composite :as composite]
            [cljfx.fx.custom-menu-item :as fx.custom-menu-item]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control SeparatorMenuItem]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.custom-menu-item/props
    (composite/props SeparatorMenuItem
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["separator-menu-item" "custom-menu-item" "menu-item"]])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe SeparatorMenuItem
      :ctor []
      :props props)
    :separator-menu-item))