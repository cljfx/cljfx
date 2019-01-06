(ns cljfx.fx.date-picker
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.combo-box-base :as fx.combo-box-base])
  (:import [javafx.scene.control DatePicker]))

(def lifecycle
  (lifecycle.composite/describe DatePicker
    :ctor []
    :extends [fx.combo-box-base/lifecycle]
    :props {:chronology [:setter lifecycle/scalar :coerce coerce/chronology :default :iso]
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                        :default :local-date]
            :day-cell-factory [:setter lifecycle/scalar :coerce coerce/cell-factory]
            :show-week-numbers [:setter lifecycle/scalar :default false]}))