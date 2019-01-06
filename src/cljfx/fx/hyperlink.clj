(ns cljfx.fx.hyperlink
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.button-base :as fx.button-base])
  (:import [javafx.scene.control Hyperlink]))

(def lifecycle
  (lifecycle.composite/describe Hyperlink
    :ctor []
    :extends [fx.button-base/lifecycle]
    :props {:visited [:setter lifecycle/scalar :default false]}))