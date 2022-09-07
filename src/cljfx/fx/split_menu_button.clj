(ns cljfx.fx.split-menu-button
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.menu-button :as fx.menu-button]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control SplitMenuButton]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.menu-button/props
    (composite/props SplitMenuButton
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "split-menu-button"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :split-menu-button]
      :mnemonic-parsing [:setter lifecycle/scalar :default true])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe SplitMenuButton
      :ctor []
      :props props)
    :split-menu-button))
