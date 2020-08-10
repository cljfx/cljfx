(ns cljfx.jdk.fx.table-column-base
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TableColumnBase]))

(set! *warn-on-reflection* true)

(def props
  (composite/props TableColumnBase
    :reorderable [:setter lifecycle/scalar :default true]))
