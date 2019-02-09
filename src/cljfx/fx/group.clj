(ns cljfx.fx.group
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene Group]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.node/props
    (composite/props Group
      :children [:list lifecycle/dynamics]
      :auto-size-children [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe Group
    :ctor []
    :props props))
