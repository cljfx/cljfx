(ns cljfx.fx.web-view
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.node :as fx.node]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.web WebView]
           [javafx.scene.text FontSmoothingType]))

(def lifecycle
  (lifecycle.composite/describe WebView
    :ctor []
    :extends [fx.node/lifecycle]
    :props {:context-menu-enabled [:setter lifecycle/scalar :default true]
            :font-scale [:setter lifecycle/scalar :coerce double :default 1.0]
            :font-smoothing-type [:setter lifecycle/scalar
                                  :coerce (coerce/enum FontSmoothingType) :default :lcd]
            :max-height [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
            :max-width [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
            :min-height [:setter lifecycle/scalar :coerce double :default 0.0]
            :min-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :pref-height [:setter lifecycle/scalar :coerce double :default 600.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default 800.0]
            :zoom [:setter lifecycle/scalar :coerce double :default 1.0]
            :url [(mutator/setter #(.load (.getEngine ^WebView %1) %2))
                  lifecycle/scalar]}))