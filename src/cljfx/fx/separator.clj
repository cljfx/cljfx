(ns cljfx.fx.separator
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Separator]
           [javafx.geometry HPos Orientation VPos]))

(def lifecycle
  (lifecycle.composite/describe Separator
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos) :default :center]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :valignment [:setter lifecycle/scalar :coerce (coerce/enum VPos) :default :center]}))