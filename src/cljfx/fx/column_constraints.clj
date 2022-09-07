(ns cljfx.fx.column-constraints
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.layout ColumnConstraints Priority]
           [javafx.geometry HPos]))

(set! *warn-on-reflection* true)

(def props
  (composite/props ColumnConstraints
    :fill-width [:setter lifecycle/scalar :default true]
    :halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos)]
    :hgrow [:setter lifecycle/scalar :coerce (coerce/enum Priority)]
    :max-width [:setter lifecycle/scalar :coerce coerce/pref-or-computed-size-double
                :default :use-computed-size]
    :min-width [:setter lifecycle/scalar :coerce coerce/pref-or-computed-size-double
                :default :use-computed-size]
    :percent-width [:setter lifecycle/scalar :coerce coerce/computed-size-double
                    :default :use-computed-size]
    :pref-width [:setter lifecycle/scalar :coerce coerce/computed-size-double
                 :default :use-computed-size]))

(def lifecycle
  (lifecycle/annotate
    (composite/describe ColumnConstraints
      :ctor []
      :props props)
    :column-constraints))
