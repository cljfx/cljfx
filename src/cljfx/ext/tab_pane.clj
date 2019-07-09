(ns cljfx.ext.tab-pane
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.ext.selection-model :as ext.selection-model])
  (:import [javafx.scene.control TabPane Tab]))

(def with-selection-props
  "Extension lifecycle providing selection-related props to tab-pane component

  Supported keys:
  - `:desc` (required) - component description whose instance has to be a TabPane
  - `:props` (optional) - map of selection-related props:
    - selection, one of:
      - `:selected-index` - int
      - `:selected-item` - a Tab, otherwise a tab description, whose instance should be present
        in the tabs list of this TabPane
    - selection change listener, one of:
      - `:on-selected-index-changed` - will receive int
      - `:on-selected-item-changed` - will receive selected Tab"
  (ext.selection-model/make-with-props
    lifecycle/dynamic
    #(.getSelectionModel ^TabPane %)
    (lifecycle/if-desc #(instance? Tab %)
      lifecycle/scalar
      lifecycle/dynamic)))
