(ns cljfx.jdk.fx.spinner
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control Spinner]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Spinner
    :initial-delay [:setter lifecycle/scalar :coerce coerce/duration :default [300 :ms]]
    :prompt-text [:setter lifecycle/scalar :default ""]
    :repeat-delay [:setter lifecycle/scalar :coerce coerce/duration :default [60 :ms]]))
