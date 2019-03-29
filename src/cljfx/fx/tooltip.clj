(ns cljfx.fx.tooltip
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce]
            [cljfx.fx.popup-control :as fx.popup-control]
            [cljfx.mutator :as mutator])
  (:import [javafx.scene.control Tooltip ContentDisplay OverrunStyle]
           [javafx.scene.text TextAlignment]))

(set! *warn-on-reflection* true)

(def ^:private install-mutator
  (reify mutator/Mutator
    (assign! [_ instance coerce value]
      (Tooltip/install (coerce value) instance))
    (replace! [_ instance coerce old-value new-value]
      (when-not (= old-value new-value)
        (Tooltip/uninstall (coerce old-value) instance)
        (Tooltip/install (coerce new-value) instance)))
    (retract! [_ instance coerce value]
      (Tooltip/uninstall (coerce value) instance))))

(def props
  (merge
    fx.popup-control/props
    (composite/props Tooltip
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "tooltip"]
      ;; definitions
      :content-display [:setter lifecycle/scalar
                        :coerce (coerce/enum ContentDisplay)
                        :default :left]
      :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
      :graphic [:setter lifecycle/dynamic]
      :graphic-text-gap [:setter lifecycle/scalar :coerce double :default 4.0]
      :hide-delay [:setter lifecycle/scalar :coerce coerce/duration :default [200 :ms]]
      :install-to [install-mutator lifecycle/dynamic]
      :show-delay [:setter lifecycle/scalar :coerce coerce/duration :default [1 :s]]
      :show-duration [:setter lifecycle/scalar :coerce coerce/duration :default [5 :s]]
      :text [:setter lifecycle/scalar :default ""]
      :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                       :default :left]
      :text-overrun [:setter lifecycle/scalar :coerce (coerce/enum OverrunStyle)
                     :default :ellipsis]
      :wrap-text [:setter lifecycle/scalar :default false])))

(def lifecycle
  (composite/describe Tooltip
    :ctor []
    :props props))
