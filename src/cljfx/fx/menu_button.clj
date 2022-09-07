(ns cljfx.fx.menu-button
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.jdk.fx.menu-button :as jdk.fx.menu-button]
            [cljfx.fx.button-base :as fx.button-base])
  (:import [javafx.scene.control MenuButton]
           [javafx.geometry Side]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.button-base/props
    jdk.fx.menu-button/props
    (composite/props MenuButton
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "menu-button"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :menu-button]
      :mnemonic-parsing [:setter lifecycle/scalar :default true]
      ;; definitions
      :items [:list lifecycle/dynamics]
      :popup-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :bottom])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe MenuButton
      :ctor []
      :props props)
    :menu-button))
