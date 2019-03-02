(ns cljfx.fx.region
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene.layout Region]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (composite/props Region
      ;; overrides
      :pick-on-bounds [:setter lifecycle/scalar :default true]
      ;; definitions
      :background [:setter lifecycle/scalar :coerce coerce/background]
      :border [:setter lifecycle/scalar :coerce coerce/border]
      :cache-shape [:setter lifecycle/scalar :default true]
      :center-shape [:setter lifecycle/scalar :default true]
      :max-height [:setter lifecycle/scalar
                   :coerce coerce/pref-or-computed-size-double
                   :default :use-computed-size]
      :max-width [:setter lifecycle/scalar
                  :coerce coerce/pref-or-computed-size-double
                  :default :use-computed-size]
      :min-height [:setter lifecycle/scalar
                   :coerce coerce/pref-or-computed-size-double
                   :default :use-computed-size]
      :min-width [:setter lifecycle/scalar
                  :coerce coerce/pref-or-computed-size-double
                  :default :use-computed-size]
      :opaque-insets [:setter lifecycle/scalar :coerce coerce/insets]
      :padding [:setter lifecycle/scalar :coerce coerce/insets :default :empty]
      :pref-height [:setter lifecycle/scalar
                    :coerce coerce/computed-size-double
                    :default :use-computed-size]
      :pref-width [:setter lifecycle/scalar
                   :coerce coerce/computed-size-double
                   :default :use-computed-size]
      :scale-shape [:setter lifecycle/scalar :default true]
      :shape [:setter lifecycle/dynamic]
      :snap-to-pixel [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe Region
    :ctor []
    :props props))
