(ns cljfx.fx.label
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.labeled :as fx.labeled])
  (:import [javafx.scene.control Label]))

(def lifecycle
  ;; TODO label has label-for prop - a component ref
  (lifecycle.composite/describe Label
    :ctor []
    :extends [fx.labeled/lifecycle]))