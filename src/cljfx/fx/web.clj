(ns cljfx.fx.web
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.control :as fx.control]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.web HTMLEditor WebView]
           [javafx.scene.text FontSmoothingType]))

(def html-editor
  (lifecycle.composite/describe HTMLEditor
    :ctor []
    :extends [fx.control/control]
    :props {:html-text
            [:setter lifecycle/scalar :default
             "<html><head></head><body contenteditable=\"true\"></body></html>"]}))

(def web-view
  (lifecycle.composite/describe WebView
    :ctor []
    :extends [fx.scene/node]
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

(def tag->lifecycle
  {:html-editor html-editor
   :web-view web-view})
