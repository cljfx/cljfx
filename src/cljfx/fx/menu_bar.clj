(ns cljfx.fx.menu-bar
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control MenuBar]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (composite/props MenuBar
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "menu-bar"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :menu-bar]
      ;; definitions
      :menus [:list lifecycle/dynamics]
      :use-system-menu-bar [:setter lifecycle/scalar :default false])))


(def lifecycle
  (composite/describe MenuBar
    :ctor []
    :props props))
