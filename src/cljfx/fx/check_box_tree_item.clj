(ns cljfx.fx.check-box-tree-item
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.tree-item :as fx.tree-item]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control CheckBoxTreeItem]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.tree-item/props
    (composite/props CheckBoxTreeItem
      :independent [:setter lifecycle/scalar :default false]
      :indeterminate [:setter lifecycle/scalar :default false]
      :selected [:setter lifecycle/scalar :default false]
      :on-selected-changed [:property-change-listener lifecycle/change-listener])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe CheckBoxTreeItem
      :ctor []
      :props props)
    :check-box-tree-item))
