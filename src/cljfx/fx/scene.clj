(ns cljfx.fx.scene
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.scene Scene]
           [javafx.geometry NodeOrientation]))

(set! *warn-on-reflection* true)

(defn- accelerators [m]
  (persistent!
    (reduce-kv
      (fn [acc k v]
        (assoc! acc (coerce/key-combination k) (coerce/runnable v)))
      (transient {})
      m)))

(def props
  (composite/props Scene
    :accelerators [:map (lifecycle/map-of lifecycle/event-handler) :coerce accelerators]
    :camera [:setter lifecycle/dynamic :default {:fx/type :parallel-camera}]
    :cursor [:setter lifecycle/scalar :coerce coerce/cursor]
    :event-dispatcher [:setter lifecycle/scalar]
    :fill [:setter lifecycle/scalar :coerce coerce/paint :default :white]
    :node-orientation [:setter lifecycle/scalar :coerce (coerce/enum NodeOrientation)]
    :on-context-menu-requested [:setter lifecycle/event-handler
                                :coerce coerce/event-handler]
    :on-drag-detected [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-drag-done [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-drag-dropped [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-drag-entered [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-drag-exited [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-drag-over [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-focus-owner-changed [:property-change-listener lifecycle/change-listener]
    :on-input-method-text-changed [:setter lifecycle/scalar :coerce coerce/event-handler]
    :on-key-pressed [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-key-released [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-key-typed [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-clicked [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-drag-entered [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-drag-exited [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-drag-over [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-drag-released [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-dragged [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-entered [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-exited [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-moved [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-pressed [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-mouse-released [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-rotate [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-rotation-finished [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-rotation-started [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-scroll [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-scroll-finished [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-scroll-started [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-swipe-down [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-swipe-left [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-swipe-right [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-swipe-up [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-touch-moved [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-touch-pressed [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-touch-released [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-touch-stationary [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-zoom [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-zoom-finished [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :on-zoom-started [:setter lifecycle/event-handler :coerce coerce/event-handler]
    :root [:setter lifecycle/dynamic]
    :stylesheets [:list lifecycle/scalar :default []]
    :user-agent-stylesheet [:setter lifecycle/scalar]
    :user-data [:setter lifecycle/scalar]))

(def lifecycle
  (composite/describe Scene
    :ctor [:root]
    :props props))
