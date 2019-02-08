(ns cljfx.fx.path
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Path FillRule]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (lifecycle.composite/props Path
      :elements [:list lifecycle/dynamics]
      :fill [:setter lifecycle/scalar :coerce coerce/paint]
      :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
      :fill-rule [:setter lifecycle/scalar :coerce (coerce/enum FillRule)
                  :default :non-zero])))

(def lifecycle
  (lifecycle.composite/describe Path
    :ctor []
    :props props))
