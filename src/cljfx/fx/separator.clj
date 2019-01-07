(ns cljfx.fx.separator
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Separator]
           [javafx.geometry HPos Orientation VPos]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Separator
    :ctor []
    :extends [fx.control/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "separator"]
            ;; definitions
            :halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos) :default :center]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :valignment [:setter lifecycle/scalar :coerce (coerce/enum VPos) :default :center]}))