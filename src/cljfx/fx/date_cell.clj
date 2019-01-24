(ns cljfx.fx.date-cell
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.cell :as fx.cell]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control DateCell]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe DateCell
    :ctor []
    :extends [fx.cell/lifecycle]
    :props {;; overrides
            :style-class [:list lifecycle/scalar :coerce coerce/style-class
                          :default ["cell" "date-cell"]]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                              :default :text]}))