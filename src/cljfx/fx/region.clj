(ns cljfx.fx.region
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene.layout Region]))

(def lifecycle
  (lifecycle.composite/describe Region
    :ctor []
    :extends [fx.node/lifecycle]
    :props {:pick-on-bounds [:setter lifecycle/scalar :default true]
            :background [:setter lifecycle/scalar :coerce coerce/background]
            :border [:setter lifecycle/scalar :coerce coerce/border]
            :cache-shape [:setter lifecycle/scalar :default true]
            :center-shape [:setter lifecycle/scalar :default true]
            :max-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :max-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :opaque-insets [:setter lifecycle/scalar :coerce coerce/insets]
            :padding [:setter lifecycle/scalar :coerce coerce/insets]
            :pref-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :scale-shape [:setter lifecycle/scalar :default true]
            :shape [:setter lifecycle/dynamic]
            :snap-to-pixel [:setter lifecycle/scalar :default true]}))