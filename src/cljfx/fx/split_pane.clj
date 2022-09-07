(ns cljfx.fx.split-pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.control SplitPane]
           [javafx.geometry Orientation]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (composite/props SplitPane
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default "split-pane"]
      ;; definitions
      :divider-positions [:setter lifecycle/scalar
                          :coerce #(into-array Double/TYPE (map double %))
                          :default []]
      :items [:list (-> lifecycle/dynamic
                        (lifecycle/wrap-extra-props
                          {:split-pane/resizable-with-parent (prop/make
                                                               (mutator/setter #(SplitPane/setResizableWithParent %1 %2))
                                                               lifecycle/scalar)})
                        lifecycle/wrap-many)]
      :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                    :default :horizontal])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe SplitPane
      :ctor []
      :props props)
    :split-pane))
