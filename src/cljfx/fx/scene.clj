(ns cljfx.fx.scene
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene Node AccessibleRole CacheHint DepthTest Group SubScene Scene]
           [javafx.scene.effect BlendMode]
           [javafx.geometry NodeOrientation]
           [javafx.scene.image ImageView]
           [javafx.scene.canvas Canvas]
           [javafx.scene.layout Region]))

(set! *warn-on-reflection* true)

(def node
  (lifecycle.composite/describe Node
    :props {:properties [(prop/observable-map #(.getProperties ^Node %))
                         prop/scalar]
            :accessible-help [:setter prop/scalar]
            :accessible-role-description [:setter prop/scalar]
            :accessible-role [:setter prop/scalar :coerce (coerce/enum AccessibleRole)]
            :blend-mode [:setter prop/scalar :coerce (coerce/enum BlendMode)]
            :cache-hint [:setter prop/scalar :coerce (coerce/enum CacheHint)
                         :default :default]
            :cache [:setter prop/scalar :default false]
            :cursor [:setter prop/scalar :coerce coerce/cursor]
            :depth-test [:setter prop/scalar :coerce (coerce/enum DepthTest)]
            :disabled [(prop/setter (lifecycle.composite/setter Node :disable)) prop/scalar]
            :effect [:setter prop/component]
            :event-dispatcher [:setter prop/scalar]
            :focus-traversable [:setter prop/scalar :default false]
            :id [:setter prop/scalar]
            :input-method-requests [:setter prop/scalar]
            :layout-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :layout-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :managed [:setter prop/scalar :default true]
            :mouse-transparent [:setter prop/scalar :default false]
            :node-orientation [:setter prop/scalar :coerce (coerce/enum NodeOrientation)
                               :default :inherit]
            :on-context-menu-requested [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-detected [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-done [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-dropped [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-entered [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-exited [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-over [:setter prop/scalar :coerce coerce/event-handler]
            :on-input-method-text-changed [:setter prop/scalar :coerce coerce/event-handler]
            :on-key-pressed [:setter prop/scalar :coerce coerce/event-handler]
            :on-key-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-key-typed [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-clicked [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-entered [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-exited [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-over [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-dragged [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-entered [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-exited [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-moved [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-pressed [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-rotate [:setter prop/scalar :coerce coerce/event-handler]
            :on-rotation-finished [:setter prop/scalar :coerce coerce/event-handler]
            :on-rotation-started [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-finished [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-started [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-down [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-left [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-right [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-up [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-moved [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-pressed [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-stationary [:setter prop/scalar :coerce coerce/event-handler]
            :on-zoom [:setter prop/scalar :coerce coerce/event-handler]
            :on-zoom-finished [:setter prop/scalar :coerce coerce/event-handler]
            :on-zoom-started [:setter prop/scalar :coerce coerce/event-handler]
            :opacity [:setter prop/scalar :coerce coerce/as-double :default 1]
            :pick-on-bounds [:setter prop/scalar :default false]
            :rotate [:setter prop/scalar :coerce coerce/as-double :default 0]
            :rotation-axis [:setter prop/scalar :coerce coerce/point-3d :default :z-axis]
            :scale-x [:setter prop/scalar :coerce coerce/as-double :default 1]
            :scale-y [:setter prop/scalar :coerce coerce/as-double :default 1]
            :scale-z [:setter prop/scalar :coerce coerce/as-double :default 1]
            :style [:setter prop/scalar :coerce coerce/style :default ""]
            :style-class [:list prop/scalar :coerce coerce/style-class]
            :transforms [:list prop/component-vec]
            :translate-x [:setter prop/scalar :coerce coerce/as-double :default 0]
            :translate-y [:setter prop/scalar :coerce coerce/as-double :default 0]
            :translate-z [:setter prop/scalar :coerce coerce/as-double :default 0]
            :view-order [:setter prop/scalar :coerce coerce/as-double :default 0]
            :visible [:setter prop/scalar :default true]}))

(def image-view
  (lifecycle.composite/describe ImageView
    :ctor []
    :extends [node]
    :default-prop [:image prop/extract-single]
    :props {:image [:setter prop/scalar :coerce coerce/image]
            :x [:setter prop/scalar :coerce coerce/as-double, :default 0
                :y [:setter prop/scalar :coerce coerce/as-double, :default 0]
                :fit-width [:setter prop/scalar :coerce coerce/as-double, :default 0]
                :fit-height [:setter prop/scalar :coerce coerce/as-double, :default 0]
                :preserve-ratio [:setter prop/scalar :default false]
                :smooth [:setter prop/scalar :default ImageView/SMOOTH_DEFAULT]
                :viewport [:setter prop/scalar :coerce coerce/rectangle-2d]]}))

(def canvas
  (lifecycle.composite/describe Canvas
    :ctor []
    :prop-order {:draw 1}
    :extends [node]
    :props {:height [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :width [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :draw [(prop/setter #(%2 %1))
                   prop/scalar
                   :default
                   (fn [^Canvas canvas]
                     (.clearRect
                       (.getGraphicsContext2D canvas)
                       0
                       0
                       (.getWidth canvas)
                       (.getHeight canvas)))]}))

(def group
  (lifecycle.composite/describe Group
    :ctor []
    :extends [node]
    :default-prop [:children prop/extract-all]
    :props {:children [:list prop/component-vec]
            :auto-size-children [:setter prop/scalar :default true]}))

(def sub-scene
  (lifecycle.composite/describe SubScene
    :ctor [:root :width :height]
    :default-prop [:root prop/extract-single]
    :extends [node]
    :props {:root [:setter prop/component]
            :width [:setter prop/scalar :coerce coerce/as-double :default 0]
            :height [:setter prop/scalar :coerce coerce/as-double :default 0]
            :camera [:setter prop/component]
            :fill [:setter prop/scalar :coerce coerce/paint]
            :user-agent-stylesheet [:setter prop/scalar]}))

(def region
  (lifecycle.composite/describe Region
    :ctor []
    :extends [node]
    :props {:pick-on-bounds [:setter prop/scalar :default true]
            :background [:setter prop/scalar :coerce coerce/background]
            :border [:setter prop/scalar :coerce coerce/border]
            :cache-shape [:setter prop/scalar :default true]
            :center-shape [:setter prop/scalar :default true]
            :max-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :max-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :min-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :min-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :opaque-insets [:setter prop/scalar :coerce coerce/insets]
            :padding [:setter prop/scalar :coerce coerce/insets]
            :pref-height [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :pref-width [:setter prop/scalar :coerce coerce/as-double :default -1.0]
            :scale-shape [:setter prop/scalar :default true]
            :shape [:setter prop/component]
            :snap-to-pixel [:setter prop/scalar :default true]}))

(def scene
  (lifecycle.composite/describe Scene
    :ctor [:root]
    :default-prop [:root prop/extract-single]
    :props {:camera [:setter prop/component :default [:camera/parallel]]
            :cursor [:setter prop/scalar :coerce coerce/cursor]
            :event-dispatcher [:setter prop/scalar]
            :fill [:setter prop/scalar :coerce coerce/paint :default :white]
            :node-orientation [:setter prop/scalar :coerce (coerce/enum NodeOrientation)]
            :on-context-menu-requested [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-detected [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-done [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-dropped [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-entered [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-exited [:setter prop/scalar :coerce coerce/event-handler]
            :on-drag-over [:setter prop/scalar :coerce coerce/event-handler]
            :on-input-method-text-changed [:setter prop/scalar
                                           :coerce coerce/event-handler]
            :on-key-pressed [:setter prop/scalar :coerce coerce/event-handler]
            :on-key-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-key-typed [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-clicked [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-entered [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-exited [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-over [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-drag-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-dragged [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-entered [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-exited [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-moved [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-pressed [:setter prop/scalar :coerce coerce/event-handler]
            :on-mouse-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-rotate [:setter prop/scalar :coerce coerce/event-handler]
            :on-rotation-finished [:setter prop/scalar :coerce coerce/event-handler]
            :on-rotation-started [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-finished [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-started [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-down [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-left [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-right [:setter prop/scalar :coerce coerce/event-handler]
            :on-swipe-up [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-moved [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-pressed [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-released [:setter prop/scalar :coerce coerce/event-handler]
            :on-touch-stationary [:setter prop/scalar :coerce coerce/event-handler]
            :on-zoom [:setter prop/scalar :coerce coerce/event-handler]
            :on-zoom-finished [:setter prop/scalar :coerce coerce/event-handler]
            :on-zoom-started [:setter prop/scalar :coerce coerce/event-handler]
            :root [:setter prop/component]
            :stylesheets [:list prop/scalar :default []]
            :user-agent-stylesheet [:setter prop/scalar]
            :user-data [:setter prop/scalar]}))

(def tag->lifecycle
  {:image-view image-view
   :canvas canvas
   :group group
   :sub-scene sub-scene
   :region region
   :scene scene})