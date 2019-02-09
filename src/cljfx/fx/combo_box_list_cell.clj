(ns cljfx.fx.combo-box-list-cell
  (:require [cljfx.composite :as composite]
            [cljfx.fx.list-cell :as fx.list-cell]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control.cell ComboBoxListCell]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.list-cell/props
    (composite/props ComboBoxListCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "list-cell" "combo-box-list-cell"]]
      ;; definitions
      :combo-box-editable [:setter lifecycle/scalar :default false]
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter])))

(def lifecycle
  (composite/describe ComboBoxListCell
    :ctor []
    :props props))
