(ns cljfx.fx.tooltip
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.popup-control :as fx.popup-control])
  (:import [javafx.scene.control Tooltip ContentDisplay OverrunStyle]
           [javafx.scene.text TextAlignment]))

(def lifecycle
  (lifecycle.composite/describe Tooltip
    :ctor []
    :extends [fx.popup-control/lifecycle]
    :props {:content-display [:setter lifecycle/scalar
                              :coerce (coerce/enum ContentDisplay)
                              :default :left]
            :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
            :graphic [:setter lifecycle/dynamic]
            :graphic-text-gap [:setter lifecycle/scalar :coerce double :default 4.0]
            :hide-delay [:setter lifecycle/scalar :coerce coerce/duration
                         :default [200 :ms]]
            :show-delay [:setter lifecycle/scalar :coerce coerce/duration
                         :default [1 :second]]
            :show-duration [:setter lifecycle/scalar :coerce coerce/duration
                            :default [5 :seconds]]
            :text [:setter lifecycle/scalar :default ""]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]
            :text-overrun [:setter lifecycle/scalar :coerce (coerce/enum OverrunStyle)
                           :default :ellipsis]
            :wrap-text [:setter lifecycle/scalar :default false]}))