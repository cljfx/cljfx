(ns cljfx.jdk.fx.text
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.text Text]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Text
    :selection-start [:setter lifecycle/scalar :coerce int :default -1]
    :selection-end [:setter lifecycle/scalar :coerce int :default -1]
    :selection-fill [:setter lifecycle/scalar :coerce coerce/paint :default :white]
    :tab-size [:setter lifecycle/scalar :coerce int :default 8]
    :caret-position [:setter lifecycle/scalar :coerce int :default -1]
    :caret-bias [:setter lifecycle/scalar :default true]))
