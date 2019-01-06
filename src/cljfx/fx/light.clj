(ns cljfx.fx.light
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect Light]))

(def lifecycle
  (lifecycle.composite/describe Light
    :props {:color [:setter lifecycle/scalar :coerce coerce/color :default :white]}))