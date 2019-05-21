(ns cljfx.ext.node
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.control Tooltip]))

(def with-tooltip-props
  "Extension lifecycle providing `:tooltip` prop to any Node

  Note that Controls already have `:tooltip` property, so this is useful only for other
  types of Nodes

  Supported keys:
  - `:desc` (required) - component description of a Node
  - `:props` (optional) - map of tooltip-related props:
    - `:tooltip` - description of a tooltip"
  (lifecycle/make-ext-with-props
    lifecycle/dynamic
    {:tooltip (prop/make
                (mutator/adder-remover
                  #(Tooltip/install %1 %2)
                  #(Tooltip/uninstall %1 %2))
                lifecycle/dynamic)}))
