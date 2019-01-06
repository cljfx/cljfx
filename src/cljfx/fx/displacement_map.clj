(ns cljfx.fx.displacement-map
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect DisplacementMap]))

(def lifecycle
  (lifecycle.composite/describe DisplacementMap
    :ctor []
    :props {:input [:setter lifecycle/dynamic]
            :map-data [:setter lifecycle/scalar :coerce coerce/float-map
                       :offset-x [:setter lifecycle/scalar :coerce double :default 0]
                       :offset-y [:setter lifecycle/scalar :coerce double :default 0]
                       :scale-x [:setter lifecycle/scalar :coerce double :default 1]
                       :scale-y [:setter lifecycle/scalar :coerce double :default 1]
                       :wrap [:setter lifecycle/scalar :default false]]}))