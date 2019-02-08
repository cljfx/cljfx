(ns cljfx.fx.displacement-map
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.effect DisplacementMap FloatMap]))

(set! *warn-on-reflection* true)

(defn- map->float-map [m]
  (let [ret (FloatMap. (:width m 1) (:height m 1))]
    (doseq [{x :x y :y [s0 s1] :s} (:samples m)]
      (.setSamples ret x y s0 s1))
    ret))

(defn- float-map [x]
  (cond
    (instance? FloatMap x) x
    (map? x) (map->float-map x)
    :else (coerce/fail FloatMap x)))

(def props
  (lifecycle.composite/props DisplacementMap
    :input [:setter lifecycle/dynamic]
    :map-data [:setter lifecycle/scalar :coerce float-map]
    :offset-x [:setter lifecycle/scalar :coerce double :default 0]
    :offset-y [:setter lifecycle/scalar :coerce double :default 0]
    :scale-x [:setter lifecycle/scalar :coerce double :default 1]
    :scale-y [:setter lifecycle/scalar :coerce double :default 1]
    :wrap [:setter lifecycle/scalar :default false]))

(def lifecycle
  (lifecycle.composite/describe DisplacementMap
    :ctor []
    :props props))
