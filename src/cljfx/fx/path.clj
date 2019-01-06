(ns cljfx.fx.path
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Path FillRule]))

(def lifecycle
  (lifecycle.composite/describe Path
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:elements [:list lifecycle/dynamics]
            :fill [:setter lifecycle/scalar :coerce coerce/paint]
            :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :fill-rule [:setter lifecycle/scalar :coerce (coerce/enum FillRule)
                        :default :non-zero]}))