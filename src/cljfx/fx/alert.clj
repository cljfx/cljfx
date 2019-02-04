(ns cljfx.fx.alert
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.fx.dialog :as fx.dialog]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.fx.dialog-pane :as fx.dialog-pane])
  (:import [javafx.scene.control Alert Alert$AlertType]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe Alert
    :ctor [:alert-type]
    :extends [fx.dialog/lifecycle]
    :props {:alert-type [:setter lifecycle/scalar :coerce (coerce/enum Alert$AlertType)]
            :button-types [:list
                           (lifecycle/wrap-many lifecycle/scalar
                                                (constantly nil)
                                                identity)
                           :coerce #(map fx.dialog-pane/button-type %)]}))
