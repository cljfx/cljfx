(ns cljfx.fx.radio-menu-item
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.menu-item :as fx.menu-item]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control RadioMenuItem]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.menu-item/props
    (composite/props RadioMenuItem
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["radio-menu-item" "menu-item"]]
      ;; definitions
      :selected [:setter lifecycle/scalar :default false]
      :on-selected-changed [:property-change-listener lifecycle/change-listener]
      :toggle-group [:setter lifecycle/dynamic])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe RadioMenuItem
      :ctor []
      :props props)
    :radio-menu-item))
