(ns cljfx.fx.web
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.control :as fx.control]
            [cljfx.prop :as prop]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.web HTMLEditor WebView]
           [javafx.scene.text FontSmoothingType]))

(def html-editor
  (lifecycle.composite/describe HTMLEditor
    :ctor []
    :extends [fx.control/control]
    :props {:html-text
            [:setter prop/scalar :default
             "<html><head></head><body contenteditable=\"true\"></body></html>"]}))

(def web-view
  (lifecycle.composite/describe WebView
    :ctor []
    :extends [fx.scene/node]
    :props {:context-menu-enabled [:setter prop/scalar :default true]
            :font-scale [:setter prop/scalar :coerce double :default 1.0]
            :font-smoothing-type [:setter prop/scalar
                                  :coerce (coerce/enum FontSmoothingType) :default :lcd]
            :max-height [:setter prop/scalar :coerce double :default Double/MAX_VALUE]
            :max-width [:setter prop/scalar :coerce double :default Double/MAX_VALUE]
            :min-height [:setter prop/scalar :coerce double :default 0.0]
            :min-width [:setter prop/scalar :coerce double :default 0.0]
            :pref-height [:setter prop/scalar :coerce double :default 600.0]
            :pref-width [:setter prop/scalar :coerce double :default 800.0]
            :zoom [:setter prop/scalar :coerce double :default 1.0]
            :url [(prop/setter #(.load (.getEngine ^WebView %1) %2))
                  prop/scalar]}))

(def tag->lifecycle
  {:html-editor html-editor
   :web-view web-view})
