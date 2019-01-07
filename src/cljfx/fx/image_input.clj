(ns cljfx.fx.image-input
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect ImageInput]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe ImageInput
    :ctor []
    :props {:source [:setter lifecycle/scalar :coerce coerce/image]
            :x [:setter lifecycle/scalar :coerce double :default 0]
            :y [:setter lifecycle/scalar :coerce double :default 0]}))