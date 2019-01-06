(ns cljfx.fx.popup-control
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.popup-window :as fx.popup-window])
  (:import [javafx.scene.control PopupControl]
           [javafx.stage PopupWindow$AnchorLocation]))

(def lifecycle
  (lifecycle.composite/describe PopupControl
    :ctor []
    :extends [fx.popup-window/lifecycle]
    :props {;; overrides
            :anchor-location [:setter lifecycle/scalar
                              :coerce (coerce/enum PopupWindow$AnchorLocation)
                              :default :content-top-left]
            ;; definitions
            :id [:setter lifecycle/scalar]
            :max-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :max-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]}))