(ns cljfx.ext.selection-model
  "Part of a public API"
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop])
  (:import [javafx.scene.control SelectionModel]))

(set! *warn-on-reflection* true)

(defn selected-index-prop [get-model]
  (prop/make
    (mutator/setter #(.clearAndSelect ^SelectionModel (get-model %1) %2))
    lifecycle/scalar))

(defn on-selected-index-changed-prop [get-model]
  (prop/make
    (mutator/property-change-listener
      #(.selectedIndexProperty
         ^SelectionModel (get-model %)))
    lifecycle/change-listener))

(defn selected-item-prop
  ([get-model] (selected-item-prop get-model lifecycle/scalar))
  ([get-model item-lifecycle]
   (prop/make
     (mutator/setter #(doto ^SelectionModel (get-model %1)
                        (.clearSelection)
                        (.select ^Object %2)))
     item-lifecycle)))

(defn on-selected-item-changed-prop [get-model]
  (prop/make
    (mutator/property-change-listener
      #(.selectedItemProperty ^SelectionModel (get-model %)))
    lifecycle/change-listener))

(defn make-with-props 
  ([lifecycle get-model] (make-with-props lifecycle get-model lifecycle/scalar))
  ([lifecycle get-model item-lifecycle]
   (lifecycle/make-ext-with-props
     lifecycle
     {:selected-index (selected-index-prop get-model)
      :on-selected-index-changed (on-selected-index-changed-prop get-model)
      :selected-item (selected-item-prop get-model item-lifecycle)
      :on-selected-item-changed (on-selected-item-changed-prop get-model)})))
