(ns cljfx.fx.svg-path
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape SVGPath FillRule]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.shape/props
    (lifecycle.composite/props SVGPath
      :content [:setter lifecycle/scalar]
      :fill-rule [:setter lifecycle/scalar :coerce (coerce/enum FillRule)
                  :default :non-zero])))

(def lifecycle
  (lifecycle.composite/describe SVGPath
    :ctor []
    :props props))
