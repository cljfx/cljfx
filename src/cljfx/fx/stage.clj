(ns cljfx.fx.stage
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce])
  (:import [javafx.stage Window PopupWindow PopupWindow$AnchorLocation Stage StageStyle]))

(set! *warn-on-reflection* true)

(def window
  (lifecycle.composite/describe Window
    :props {:event-dispatcher [:setter prop/scalar]
            :force-integer-render-scale [:setter prop/scalar :default false]
            :height [:setter prop/scalar :coerce coerce/as-double :default Double/NaN]
            :on-close-request [:setter prop/scalar :coerce coerce/event-handler]
            :on-hidden [:setter prop/scalar :coerce coerce/event-handler]
            :on-hiding [:setter prop/scalar :coerce coerce/event-handler]
            :on-showing [:setter prop/scalar :coerce coerce/event-handler]
            :on-shown [:setter prop/scalar :coerce coerce/event-handler]
            :opacity [:setter prop/scalar :coerce coerce/as-double :default 1]
            :render-scale-x [:setter prop/scalar :coerce coerce/as-double :default 1]
            :render-scale-y [:setter prop/scalar :coerce coerce/as-double :default 1]
            :user-data [:setter prop/scalar]
            :width [:setter prop/scalar :coerce coerce/as-double :default Double/NaN]
            :x [:setter prop/scalar :coerce coerce/as-double :default Double/NaN]
            :y [:setter prop/scalar :coerce coerce/as-double :default Double/NaN]}))

(def popup-window
  (lifecycle.composite/describe PopupWindow
    :extends [window]
    :props {:anchor-location [:setter prop/scalar
                              :coerce (coerce/enum PopupWindow$AnchorLocation)
                              :default :window-top-left]
            :anchor-x [:setter prop/scalar :coerce coerce/as-double :default Double/NaN]
            :anchor-y [:setter prop/scalar :coerce coerce/as-double :default Double/NaN]
            :auto-fix [:setter prop/scalar :default true]
            :auto-hide [:setter prop/scalar :default false]
            :consume-auto-hiding-events [:setter prop/scalar :default true]
            :hide-on-escape [:setter prop/scalar :default true]
            :on-auto-hide [:setter prop/scalar :coerce coerce/event-handler]}))

(def stage
  (lifecycle.composite/describe Stage
    :ctor []
    :prop-order {:showing 1}
    :default-prop [:scene prop/extract-single]
    :on-delete #(.hide ^Stage %)
    :props {:always-on-top [:setter prop/scalar :default false]
            :full-screen [:setter prop/scalar :default false]
            :full-screen-exit-hint [:setter prop/scalar]
            :full-screen-exit-key-combination [:setter prop/scalar :coerce coerce/key-combination]
            :iconified [:setter prop/scalar :default false]
            :icons [:list prop/scalar :coerce (fn [x _] (map coerce/image x))]
            :max-height [:setter prop/scalar :coerce coerce/as-double :default Double/MAX_VALUE]
            :max-width [:setter prop/scalar :coerce coerce/as-double :default Double/MAX_VALUE]
            :maximized [:setter prop/scalar :default false]
            :min-height [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :min-width [:setter prop/scalar :coerce coerce/as-double :default 0.0]
            :resizable [:setter prop/scalar :default true]
            :scene [:setter prop/component]
            :title [:setter prop/scalar]
            :style [(prop/setter #(.initStyle ^Stage %1 %2))
                    prop/scalar
                    :coerce (coerce/enum StageStyle)
                    :default :decorated]
            :showing [(prop/setter #(if %2 (.show ^Stage %1) (.hide ^Stage %1)))
                      prop/scalar
                      :default false]}))

(def tag->lifecycle
  {:stage stage})