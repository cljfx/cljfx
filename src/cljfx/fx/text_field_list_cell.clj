(ns cljfx.fx.text-field-list-cell
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.fx.list-cell :as fx.list-cell]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control.cell TextFieldListCell]))

(set! *warn-on-reflection* true)

(def props
  (merge
    fx.list-cell/props
    (composite/props TextFieldListCell
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class
                    :default ["cell" "indexed-cell" "list-cell" "text-field-list-cell"]]
      ;; definitions
      :converter [:setter lifecycle/scalar :coerce coerce/string-converter])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe TextFieldListCell
      :ctor []
      :props props)
    :text-field-list-cell))

;; proxy-super uses reflection because updateItem is protected

(set! *warn-on-reflection* false)

(defn create [f]
  (let [*props (volatile! {})]
    (proxy [TextFieldListCell] []
      (updateItem [item empty]
        (let [^TextFieldListCell this this
              props @*props]
          (proxy-super updateItem item empty)
          (vreset! *props (f (if empty props (dissoc props :text :graphic)) this item empty)))))))
