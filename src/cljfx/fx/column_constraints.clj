(ns cljfx.fx.column-constraints
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.layout ColumnConstraints Priority]
           [javafx.geometry HPos]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe ColumnConstraints
    :ctor []
    :props {:fill-width [:setter lifecycle/scalar :default true]
            :halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos)]
            :hgrow [:setter lifecycle/scalar :coerce (coerce/enum Priority)]
            :max-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :percent-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default -1.0]}))