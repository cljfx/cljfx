(ns cljfx.fx.value-axis
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.axis :as fx.axis])
  (:import [javafx.scene.chart ValueAxis]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.axis/props
    (composite/props ValueAxis
      :minor-tick-visible [:setter lifecycle/scalar :default true]
      :lower-bound [:setter lifecycle/scalar :coerce double :default 0]
      :upper-bound [:setter lifecycle/scalar :coerce double :default 100]
      :minor-tick-count [:setter lifecycle/scalar :coerce int :default 5]
      :minor-tick-length [:setter lifecycle/scalar :coerce double :default 5]
      :tick-label-formatter [:setter lifecycle/scalar :coerce coerce/string-converter])))
