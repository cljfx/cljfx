(ns cljfx.fx.node
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.jdk.fx.node :as jdk.fx.node]
            [cljfx.mutator :as mutator])
  (:import [javafx.event Event EventHandler]
           [javafx.scene Node AccessibleRole CacheHint DepthTest]
           [javafx.scene.effect BlendMode]
           [javafx.geometry NodeOrientation]))

(set! *warn-on-reflection* true)

(def props
  (merge
    jdk.fx.node/props
    (composite/props Node
      :accessible-help [:setter lifecycle/scalar]
      :accessible-role-description [:setter lifecycle/scalar]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)]
      :blend-mode [:setter lifecycle/scalar :coerce (coerce/enum BlendMode)]
      :cache-hint [:setter lifecycle/scalar :coerce (coerce/enum CacheHint)
                   :default :default]
      :cache [:setter lifecycle/scalar :default false]
      :clip [:setter lifecycle/dynamic]
      :cursor [:setter lifecycle/scalar :coerce coerce/cursor]
      :depth-test [:setter lifecycle/scalar :coerce (coerce/enum DepthTest)]
      :disable [:setter lifecycle/scalar :default false]
      :effect [:setter lifecycle/dynamic]
      :event-dispatcher [:setter lifecycle/scalar]
      ; takes an event description that receives filtered Events with event type Event/ANY
      :event-filter [(mutator/adder-remover
                       #(.addEventFilter ^Node %1 Event/ANY ^EventHandler %2)
                       #(.removeEventFilter ^Node %1 Event/ANY ^EventHandler %2))
                     (lifecycle/wrap-coerce lifecycle/event-handler coerce/event-handler)]
      :event-handler [(mutator/adder-remover
                        #(.addEventHandler ^Node %1 Event/ANY ^EventHandler %2)
                        #(.removeEventHandler ^Node %1 Event/ANY ^EventHandler %2))
                      (lifecycle/wrap-coerce lifecycle/event-handler coerce/event-handler)]
      :focus-traversable [:setter lifecycle/scalar :default false]
      :id [:setter lifecycle/scalar]
      :input-method-requests [:setter lifecycle/scalar]
      :layout-x [:setter lifecycle/scalar :coerce double :default 0]
      :layout-y [:setter lifecycle/scalar :coerce double :default 0]
      :managed [:setter lifecycle/scalar :default true]
      :mouse-transparent [:setter lifecycle/scalar :default false]
      :node-orientation [:setter lifecycle/scalar :coerce (coerce/enum NodeOrientation)
                         :default :inherit]
      :on-context-menu-requested [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-drag-detected [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-drag-done [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-drag-dropped [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-drag-entered [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-drag-exited [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-drag-over [:setter lifecycle/event-handler :coerce coerce/event-handler]
      :on-focused-changed [:property-change-listener lifecycle/change-listener]
      :on-input-method-text-changed [:setter lifecycle/event-handler :coerce coerce/event-handler]
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
      :opacity [:setter lifecycle/scalar :coerce double :default 1]
      :pick-on-bounds [:setter lifecycle/scalar :default false]
      :rotate [:setter lifecycle/scalar :coerce double :default 0]
      :rotation-axis [:setter lifecycle/scalar :coerce coerce/point-3d :default :z-axis]
      :scale-x [:setter lifecycle/scalar :coerce double :default 1]
      :scale-y [:setter lifecycle/scalar :coerce double :default 1]
      :scale-z [:setter lifecycle/scalar :coerce double :default 1]
      :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
      :style-class [:list lifecycle/scalar :coerce coerce/style-class]
      :transforms [:list lifecycle/dynamics]
      :translate-x [:setter lifecycle/scalar :coerce double :default 0]
      :translate-y [:setter lifecycle/scalar :coerce double :default 0]
      :translate-z [:setter lifecycle/scalar :coerce double :default 0]
      :visible [:setter lifecycle/scalar :default true])))
