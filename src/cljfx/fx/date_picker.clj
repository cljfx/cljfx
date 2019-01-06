(ns cljfx.fx.date-picker
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.combo-box-base :as fx.combo-box-base])
  (:import [javafx.scene.control DatePicker]
           [java.time.chrono Chronology IsoChronology HijrahChronology JapaneseChronology
                             MinguoChronology ThaiBuddhistChronology]))

(defn- chronology [x]
  (if (instance? Chronology x)
    x
    (case x
      :iso IsoChronology/INSTANCE
      :hijrah HijrahChronology/INSTANCE
      :japanese JapaneseChronology/INSTANCE
      :minguo MinguoChronology/INSTANCE
      :thai-buddhist ThaiBuddhistChronology/INSTANCE
      (coerce/fail Chronology x))))

(def lifecycle
  (lifecycle.composite/describe DatePicker
    :ctor []
    :extends [fx.combo-box-base/lifecycle]
    :props {:chronology [:setter lifecycle/scalar :coerce chronology :default :iso]
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                        :default :local-date]
            :day-cell-factory [:setter lifecycle/scalar :coerce coerce/cell-factory]
            :show-week-numbers [:setter lifecycle/scalar :default false]}))