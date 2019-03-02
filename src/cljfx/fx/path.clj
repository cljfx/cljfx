(ns cljfx.fx.path
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape Path FillRule]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (composite/props Path
      :elements [:list lifecycle/dynamics]
      :fill [:setter lifecycle/scalar :coerce coerce/paint]
      :stroke [:setter lifecycle/scalar :coerce coerce/paint :default :black]
      :fill-rule [:setter lifecycle/scalar :coerce (coerce/enum FillRule)
                  :default :non-zero])))

(def lifecycle
  (composite/describe Path
    :ctor []
    :props props))
