(ns cljfx.fx.combo-box
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.combo-box-base :as fx.combo-box-base])
  (:import [javafx.scene.control ComboBox]))

(def lifecycle
  (lifecycle.composite/describe ComboBox
    :ctor []
    :extends [fx.combo-box-base/lifecycle]
    :props {:button-cell [:setter lifecycle/dynamic]
            :cell-factory [:setter lifecycle/scalar :coerce coerce/cell-factory]
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                        :default :default]
            :items [:list lifecycle/scalar]
            :placeholder [:setter lifecycle/dynamic]
            :visible-row-count [:setter lifecycle/scalar :coerce int :default 10]}))