(ns cljfx.fx.spinner
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control Spinner]))

(def lifecycle
  (lifecycle.composite/describe Spinner
    :ctor []
    :extends [fx.control/lifecycle]
    :props {:editable [:setter lifecycle/scalar :default false]
            :initial-delay [:setter lifecycle/scalar :coerce coerce/duration :default [300 :ms]]
            :prompt-text [:setter lifecycle/scalar :default ""]
            :repeat-delay [:setter lifecycle/scalar :default [60 :ms]]
            :value-factory [:setter lifecycle/dynamic]}))