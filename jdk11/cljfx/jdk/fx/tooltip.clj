(ns cljfx.jdk.fx.tooltip
  (:require [cljfx.composite :as composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene.control Tooltip]))

(set! *warn-on-reflection* true)

(def props
  (composite/props Tooltip
    :hide-delay [:setter lifecycle/scalar :coerce coerce/duration :default [200 :ms]]
    :show-delay [:setter lifecycle/scalar :coerce coerce/duration :default [1 :s]]
    :show-duration [:setter lifecycle/scalar :coerce coerce/duration :default [5 :s]]))