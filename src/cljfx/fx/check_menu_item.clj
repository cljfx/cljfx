(ns cljfx.fx.check-menu-item
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.menu-item :as fx.menu-item]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control CheckMenuItem]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.menu-item/props
    (composite/props CheckMenuItem
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["check-menu-item" "menu-item"]]
      ;; definitions
      :selected [:setter lifecycle/scalar :default false])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe CheckMenuItem
      :ctor []
      :props props)
    :check-menu-item))
