(ns cljfx.fx.popup
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.popup-window :as fx.popup-window]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.stage Popup]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.popup-window/props
    (composite/props Popup
      :content [:list lifecycle/dynamics])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Popup
      :ctor []
      :props props)
    :popup))
