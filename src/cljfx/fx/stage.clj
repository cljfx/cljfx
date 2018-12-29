(ns cljfx.fx.stage
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.prop :as prop]
            [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
  (:import [javafx.stage Window PopupWindow PopupWindow$AnchorLocation Stage StageStyle]))

(set! *warn-on-reflection* true)

(def window
  (lifecycle.composite/describe Window
    :props {:event-dispatcher [:setter lifecycle/scalar]
            :force-integer-render-scale [:setter lifecycle/scalar :default false]
            :height [:setter lifecycle/scalar :coerce double :default Double/NaN]
            :on-close-request [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :opacity [:setter lifecycle/scalar :coerce double :default 1]
            :render-scale-x [:setter lifecycle/scalar :coerce double :default 1]
            :render-scale-y [:setter lifecycle/scalar :coerce double :default 1]
            :user-data [:setter lifecycle/scalar]
            :width [:setter lifecycle/scalar :coerce double :default Double/NaN]
            :x [:setter lifecycle/scalar :coerce double :default Double/NaN]
            :y [:setter lifecycle/scalar :coerce double :default Double/NaN]}))

(def popup-window
  (lifecycle.composite/describe PopupWindow
    :extends [window]
    :props {:anchor-location [:setter lifecycle/scalar
                              :coerce (coerce/enum PopupWindow$AnchorLocation)
                              :default :window-top-left]
            :anchor-x [:setter lifecycle/scalar :coerce double :default Double/NaN]
            :anchor-y [:setter lifecycle/scalar :coerce double :default Double/NaN]
            :auto-fix [:setter lifecycle/scalar :default true]
            :auto-hide [:setter lifecycle/scalar :default false]
            :consume-auto-hiding-events [:setter lifecycle/scalar :default true]
            :hide-on-escape [:setter lifecycle/scalar :default true]
            :on-auto-hide [:setter lifecycle/event-handler :coerce coerce/event-handler]}))

(def stage
  (-> Stage
      (lifecycle.composite/describe
        :ctor []
        :extends [window]
        :prop-order {:showing 1}
        :default-prop [:scene prop/extract-single]
        :props {:always-on-top [:setter lifecycle/scalar :default false]
                :full-screen [:setter lifecycle/scalar :default false]
                :full-screen-exit-hint [:setter lifecycle/scalar]
                :full-screen-exit-key-combination [:setter lifecycle/scalar
                                                   :coerce coerce/key-combination]
                :iconified [:setter lifecycle/scalar :default false]
                :icons [:list lifecycle/scalar :coerce (fn [x _] (map coerce/image x))]
                :max-height [:setter lifecycle/scalar :coerce double
                             :default Double/MAX_VALUE]
                :max-width [:setter lifecycle/scalar :coerce double
                            :default Double/MAX_VALUE]
                :maximized [:setter lifecycle/scalar :default false]
                :min-height [:setter lifecycle/scalar :coerce double :default 0.0]
                :min-width [:setter lifecycle/scalar :coerce double :default 0.0]
                :resizable [:setter lifecycle/scalar :default true]
                :scene [:setter lifecycle/dynamic-hiccup]
                :title [:setter lifecycle/scalar]
                :style [(mutator/setter #(.initStyle ^Stage %1 %2))
                        lifecycle/scalar
                        :coerce (coerce/enum StageStyle)
                        :default :decorated]
                :showing [(mutator/setter #(if %2 (.show ^Stage %1) (.hide ^Stage %1)))
                          lifecycle/scalar
                          :default false]})
      (lifecycle/wrap-on-delete #(.hide ^Stage %))))

(def tag->lifecycle
  {:stage stage})