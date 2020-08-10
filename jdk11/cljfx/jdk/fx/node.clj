(ns cljfx.jdk.fx.node
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene Node]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Node
    :view-order [:setter lifecycle/scalar :coerce double :default 0]))

