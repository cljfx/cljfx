(ns cljfx.fx.scroll-pane
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.control :as fx.control])
  (:import [javafx.scene.control ScrollPane ScrollPane$ScrollBarPolicy]
           [javafx.geometry Bounds BoundingBox]
           [javafx.scene AccessibleRole]))

(set! *warn-on-reflection* true)

(defn- bounds [x]
  (cond
    (instance? Bounds x)
    x

    (= 0 x)
    (BoundingBox. 0.0 0.0 0.0 0.0 0.0 0.0)

    (and (vector? x) (= 4 (count x)))
    (let [[x y w h] x]
      (BoundingBox. x y w h))

    (and (vector? x) (= 6 (count x)))
    (let [[x y z w h d] x]
      (BoundingBox. x y z w h d))

    :else
    (coerce/fail Bounds x)))

(def props
  (merge
    fx.control/props
    (composite/props ScrollPane
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "scroll-pane"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :scroll-pane]
      ;; definitions
      :content [:setter lifecycle/dynamic]
      :fit-to-height [:setter lifecycle/scalar :default false]
      :fit-to-width [:setter lifecycle/scalar :default false]
      :hbar-policy [:setter lifecycle/scalar
                    :coerce (coerce/enum ScrollPane$ScrollBarPolicy)
                    :default :as-needed]
      :hmax [:setter lifecycle/scalar :coerce double :default 1.0]
      :hmin [:setter lifecycle/scalar :coerce double :default 0.0]
      :hvalue [:setter lifecycle/scalar :coerce double :default 0.0]
      :on-hvalue-changed [:property-change-listener lifecycle/change-listener]
      :min-viewport-height [:setter lifecycle/scalar :coerce double :default 0.0]
      :min-viewport-width [:setter lifecycle/scalar :coerce double :default 0.0]
      :pannable [:setter lifecycle/scalar :default false]
      :pref-viewport-height [:setter lifecycle/scalar :coerce double :default 0.0]
      :pref-viewport-width [:setter lifecycle/scalar :coerce double :default 0.0]
      :vbar-policy [:setter lifecycle/scalar
                    :coerce (coerce/enum ScrollPane$ScrollBarPolicy)
                    :default :as-needed]
      :viewport-bounds [:setter lifecycle/scalar :coerce bounds :default 0]
      :vmax [:setter lifecycle/scalar :coerce double :default 1.0]
      :vmin [:setter lifecycle/scalar :coerce double :default 0.0]
      :vvalue [:setter lifecycle/scalar :coerce double :default 0.0]
      :on-vvalue-changed [:property-change-listener lifecycle/change-listener])))

(def lifecycle
  (composite/describe ScrollPane
    :ctor []
    :props props))
