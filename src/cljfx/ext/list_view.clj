(ns cljfx.ext.list-view
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.ext.multiple-selection-model :as ext.multiple-selection-model])
  (:import [javafx.scene.control ListView]))

(set! *warn-on-reflection* true)

(def with-selection-props
  "Extension lifecycle providing selection-related props to list-view component

  Supported keys:
  - `:desc` (required) - component description whose instance has to be a ListView
  - `:props` (optional) - map of selection-related props:
    - `:selection-mode` - either `:single` or `:multiple`
    - selection, one of:
      - `:selected-index` - int
      - `:selected-item` - value from `:items` prop
      - `:selected-indices` - coll of ints, prefer this when selection mode is `:multiple`
      - `:selected-items` - coll of values from `:items` prop, prefer this when selection
        mode is `:multiple`
    - selection change listener, one of:
      - `:on-selected-index-changed` - will receive int
      - `:on-selected-item-changed` - will receive selected value
      - `:on-selected-indices-changed` - will receive vector of ints
      - `:on-selected-items-changed` - will receive vector of selected items"
  (ext.multiple-selection-model/make-with-props
    lifecycle/dynamic
    #(.getSelectionModel ^ListView %)
    lifecycle/scalar
    :single))
