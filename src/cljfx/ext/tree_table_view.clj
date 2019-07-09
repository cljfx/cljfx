(ns cljfx.ext.tree-table-view
  (:require [cljfx.ext.multiple-selection-model :as ext.multiple-selection-model]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TreeTableView TreeItem]))

(def with-selection-props
  "Extension lifecycle providing selection-related props to tree-table-view component

  Supported keys:
  - `:desc` (required) - component description whose instance has to be a TreeTableView
  - `:props` (optional) - map of selection-related props:
    - `:selection-mode` - either `:single` or `:multiple`
    - selection, one of:
      - `:selected-index` - int
      - `:selected-item` - a TreeItem, otherwise a tree item description
      - `:selected-indices` - coll of ints, prefer this when selection mode is `:multiple`
      - `:selected-items` - coll of descriptions of tree items, prefer this when selection
        mode is `:multiple`
    - selection change listener, one of:
      - `:on-selected-index-changed` - will receive int
      - `:on-selected-item-changed` - will receive TreeItem
      - `:on-selected-indices-changed` - will receive vector of ints
      - `:on-selected-items-changed` - will receive vector of TreeItems"
  (ext.multiple-selection-model/make-with-props
    lifecycle/dynamic
    #(.getSelectionModel ^TreeTableView %)
    (lifecycle/if-desc #(instance? TreeItem %)
      lifecycle/scalar
      lifecycle/dynamic)
    lifecycle/dynamics
    :single))
