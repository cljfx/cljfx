(ns cljfx.fx.scene
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene Node AccessibleRole CacheHint DepthTest Group SubScene Scene]
           [javafx.scene.effect BlendMode]
           [javafx.geometry NodeOrientation]
           [javafx.scene.image ImageView]
           [javafx.scene.canvas Canvas]
           [javafx.scene.layout Region]))

(set! *warn-on-reflection* true)

(def node
  (lifecycle.composite/describe Node
    :props {:properties [(mutator/observable-map #(.getProperties ^Node %))
                         lifecycle/scalar]
            :accessible-help [:setter lifecycle/scalar]
            :accessible-role-description [:setter lifecycle/scalar]
            :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)]
            :blend-mode [:setter lifecycle/scalar :coerce (coerce/enum BlendMode)]
            :cache-hint [:setter lifecycle/scalar :coerce (coerce/enum CacheHint)
                         :default :default]
            :cache [:setter lifecycle/scalar :default false]
            :cursor [:setter lifecycle/scalar :coerce coerce/cursor]
            :depth-test [:setter lifecycle/scalar :coerce (coerce/enum DepthTest)]
            :disabled [(mutator/setter (lifecycle.composite/setter Node :disable))
                       lifecycle/scalar]
            :effect [:setter lifecycle/hiccup]
            :event-dispatcher [:setter lifecycle/scalar]
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
            :transforms [:list lifecycle/hiccups]
            :translate-x [:setter lifecycle/scalar :coerce double :default 0]
            :translate-y [:setter lifecycle/scalar :coerce double :default 0]
            :translate-z [:setter lifecycle/scalar :coerce double :default 0]
            :view-order [:setter lifecycle/scalar :coerce double :default 0]
            :visible [:setter lifecycle/scalar :default true]}))

(def image-view
  (lifecycle.composite/describe ImageView
    :ctor []
    :extends [node]
    :default-prop [:image prop/extract-single]
    :props {:image [:setter lifecycle/scalar :coerce coerce/image]
            :x [:setter lifecycle/scalar :coerce double, :default 0
                :y [:setter lifecycle/scalar :coerce double, :default 0]
                :fit-width [:setter lifecycle/scalar :coerce double, :default 0]
                :fit-height [:setter lifecycle/scalar :coerce double, :default 0]
                :preserve-ratio [:setter lifecycle/scalar :default false]
                :smooth [:setter lifecycle/scalar :default ImageView/SMOOTH_DEFAULT]
                :viewport [:setter lifecycle/scalar :coerce coerce/rectangle-2d]]}))

(def canvas
  (lifecycle.composite/describe Canvas
    :ctor []
    :prop-order {:draw 1}
    :extends [node]
    :props {:height [:setter lifecycle/scalar :coerce double :default 0.0]
            :width [:setter lifecycle/scalar :coerce double :default 0.0]
            :draw [(mutator/setter #(%2 %1))
                   lifecycle/scalar
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
    :props {:children [:list lifecycle/hiccups]
            :auto-size-children [:setter lifecycle/scalar :default true]}))

(def sub-scene
  (lifecycle.composite/describe SubScene
    :ctor [:root :width :height]
    :default-prop [:root prop/extract-single]
    :extends [node]
    :props {:root [:setter lifecycle/hiccup]
            :width [:setter lifecycle/scalar :coerce double :default 0]
            :height [:setter lifecycle/scalar :coerce double :default 0]
            :camera [:setter lifecycle/hiccup]
            :fill [:setter lifecycle/scalar :coerce coerce/paint]
            :user-agent-stylesheet [:setter lifecycle/scalar]}))

(def region
  (lifecycle.composite/describe Region
    :ctor []
    :extends [node]
    :props {:pick-on-bounds [:setter lifecycle/scalar :default true]
            :background [:setter lifecycle/scalar :coerce coerce/background]
            :border [:setter lifecycle/scalar :coerce coerce/border]
            :cache-shape [:setter lifecycle/scalar :default true]
            :center-shape [:setter lifecycle/scalar :default true]
            :max-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :max-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :opaque-insets [:setter lifecycle/scalar :coerce coerce/insets]
            :padding [:setter lifecycle/scalar :coerce coerce/insets]
            :pref-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :scale-shape [:setter lifecycle/scalar :default true]
            :shape [:setter lifecycle/hiccup]
            :snap-to-pixel [:setter lifecycle/scalar :default true]}))

(def scene
  (lifecycle.composite/describe Scene
    :ctor [:root]
    :default-prop [:root prop/extract-single]
    :props {:camera [:setter lifecycle/hiccup :default [:camera/parallel]]
            :cursor [:setter lifecycle/scalar :coerce coerce/cursor]
            :event-dispatcher [:setter lifecycle/scalar]
            :fill [:setter lifecycle/scalar :coerce coerce/paint :default :white]
            :node-orientation [:setter lifecycle/scalar :coerce (coerce/enum NodeOrientation)]
            :on-context-menu-requested [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-drag-detected [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-drag-done [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-drag-dropped [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-drag-entered [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-drag-exited [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-drag-over [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-input-method-text-changed [:setter lifecycle/scalar
                                           :coerce coerce/event-handler]
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
            :root [:setter lifecycle/hiccup]
            :stylesheets [:list lifecycle/scalar :default []]
            :user-agent-stylesheet [:setter lifecycle/scalar]
            :user-data [:setter lifecycle/scalar]}))

(def tag->lifecycle
  {:image-view image-view
   :canvas canvas
   :group group
   :sub-scene sub-scene
   :region region
   :scene scene})