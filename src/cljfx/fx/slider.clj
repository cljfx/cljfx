(ns cljfx.fx.slider
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.mutator :as mutator]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Slider]
           [javafx.geometry Orientation]))

(def lifecycle
  (lifecycle.composite/describe Slider
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:block-increment [:setter lifecycle/scalar :coerce double :default 10.0]
            :label-formatter [:setter lifecycle/scalar :coerce coerce/string-converter]
            :major-tick-unit [:setter lifecycle/scalar :coerce double :default 25.0]
            :max [:setter lifecycle/scalar :coerce double :default 100.0]
            :min [:setter lifecycle/scalar :coerce double :default 0.0]
            :minor-tick-count [:setter lifecycle/scalar :coerce int :default 3]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :show-tick-labels [:setter lifecycle/scalar :default false]
            :show-tick-marks [:setter lifecycle/scalar :default false]
            :snap-to-ticks [:setter lifecycle/scalar :default false]
            :value [:setter lifecycle/scalar :coerce double :default 0.0]
            :on-value-changed [(mutator/property-change-listener
                                 #(.valueProperty ^Slider %))
                               (lifecycle/wrap-coerce lifecycle/event-handler
                                                      coerce/change-listener)]
            :value-changing [:setter lifecycle/scalar :default false]}))