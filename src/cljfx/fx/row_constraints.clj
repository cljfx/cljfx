(ns cljfx.fx.row-constraints
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.layout RowConstraints Priority]
           [javafx.geometry VPos]))

(def lifecycle
  (lifecycle.composite/describe RowConstraints
    :ctor []
    :props {:fill-height [:setter lifecycle/scalar :default true]
            :max-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :percent-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :valignment [:setter lifecycle/scalar :coerce (coerce/enum VPos)]
            :vgrow [:setter lifecycle/scalar :coerce (coerce/enum Priority)]}))