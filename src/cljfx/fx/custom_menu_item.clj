(ns cljfx.fx.custom-menu-item
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.menu-item :as fx.menu-item]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control CustomMenuItem]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.menu-item/props
    (composite/props CustomMenuItem
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["custom-menu-item" "menu-item"]]
      ;; definitions
      :content [:setter lifecycle/dynamic]
      :hide-on-click [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe CustomMenuItem
    :ctor []
    :props props))
