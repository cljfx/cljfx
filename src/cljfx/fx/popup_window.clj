(ns cljfx.fx.popup-window
  (:require [cljfx.composite :as composite]
            [cljfx.fx.window :as fx.window]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.stage PopupWindow PopupWindow$AnchorLocation]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.window/props
    (composite/props PopupWindow
      :anchor-location [:setter lifecycle/scalar
                        :coerce (coerce/enum PopupWindow$AnchorLocation)
                        :default :window-top-left]
      :anchor-x [:setter lifecycle/scalar :coerce double :default Double/NaN]
      :anchor-y [:setter lifecycle/scalar :coerce double :default Double/NaN]
      :auto-fix [:setter lifecycle/scalar :default true]
      :auto-hide [:setter lifecycle/scalar :default false]
      :consume-auto-hiding-events [:setter lifecycle/scalar :default true]
      :hide-on-escape [:setter lifecycle/scalar :default true]
      :on-auto-hide [:setter lifecycle/event-handler :coerce coerce/event-handler])))
