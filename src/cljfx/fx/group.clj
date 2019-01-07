(ns cljfx.fx.group
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.node :as fx.node])
  (:import [javafx.scene Group]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Group
    :ctor []
    :extends [fx.node/lifecycle]
    :props {:children [:list lifecycle/dynamics]
            :auto-size-children [:setter lifecycle/scalar :default true]}))