(ns cljfx.fx.popup-control
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.popup-window :as fx.popup-window])
  (:import [javafx.scene.control PopupControl]
           [javafx.stage PopupWindow$AnchorLocation]))

(set! *warn-on-reflection* true)

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
            :pref-height [:setter lifecycle/scalar
                          :coerce coerce/computed-size-double
                          :default :use-computed-size]
            :pref-width [:setter lifecycle/scalar
                         :coerce coerce/computed-size-double
                         :default :use-computed-size]
            :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]}))