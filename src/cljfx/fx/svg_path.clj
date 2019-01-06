(ns cljfx.fx.svg-path
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.shape :as fx.shape])
  (:import [javafx.scene.shape SVGPath FillRule]))

(def lifecycle
  (lifecycle.composite/describe SVGPath
    :ctor []
    :extends [fx.shape/lifecycle]
    :props {:content [:setter lifecycle/scalar]
            :fill-rule [:setter lifecycle/scalar
                        :coerce (coerce/enum FillRule)
                        :default :non-zero]}))