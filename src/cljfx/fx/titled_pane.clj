(ns cljfx.fx.titled-pane
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.labeled :as fx.labeled]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control TitledPane]))

(def lifecycle
  (lifecycle.composite/describe TitledPane
    :ctor []
    :extends [fx.labeled/lifecycle]
    :props {:animated [:setter lifecycle/scalar :default true]
            :collapsible [:setter lifecycle/scalar :default true]
            :content [:setter lifecycle/dynamic]
            :expanded [:setter lifecycle/scalar :default true]}))