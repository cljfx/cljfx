(ns cljfx.fx.group
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.parent :as fx.parent])
  (:import [javafx.scene Group]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.parent/props
    (composite/props Group
      :children [:list lifecycle/dynamics]
      :auto-size-children [:setter lifecycle/scalar :default true])))

(def lifecycle
  (composite/describe Group
    :ctor []
    :props props))
