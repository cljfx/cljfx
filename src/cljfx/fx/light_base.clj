(ns cljfx.fx.light-base
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.node :as fx.node]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene LightBase]))

(def lifecycle
  (lifecycle.composite/describe LightBase
    :extends [fx.node/lifecycle]
    :props {:color [:setter lifecycle/scalar :coerce coerce/color :default :white]
            :light-on [:setter lifecycle/scalar :default true]}))