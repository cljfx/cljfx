(ns cljfx.fx.labeled
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.control :as fx.control]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Labeled ContentDisplay OverrunStyle]
           [javafx.geometry Pos]
           [javafx.scene.text TextAlignment]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.control/props
    (lifecycle.composite/props Labeled
      :alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos)
                  :default :center-left]
      :content-display [:setter lifecycle/scalar :coerce (coerce/enum ContentDisplay)
                        :default :left]
      :ellipsis-string [:setter lifecycle/scalar :default "..."]
      :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
      :graphic [:setter lifecycle/dynamic]
      :graphic-text-gap [:setter lifecycle/scalar :coerce double :default 4]
      :line-spacing [:setter lifecycle/scalar :coerce double :default 0]
      :mnemonic-parsing [:setter lifecycle/scalar :default false]
      :text [:setter lifecycle/scalar :default ""]
      :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                       :default :left]
      :text-fill [:setter lifecycle/scalar :coerce coerce/paint :default :black]
      :text-overrun [:setter lifecycle/scalar :coerce (coerce/enum OverrunStyle)
                     :default :ellipsis]
      :underline [:setter lifecycle/scalar :default false]
      :wrap-text [:setter lifecycle/scalar :default false])))
