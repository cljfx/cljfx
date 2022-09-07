(ns cljfx.ext.multiple-selection-model
  (:require [cljfx.coerce :as coerce]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]
            [cljfx.ext.selection-model :as ext.selection-model])
  (:import [javafx.scene.control MultipleSelectionModel SelectionMode]))

(set! *warn-on-reflection* true)

(defn selected-indices-prop [get-model]
  (prop/make
    (mutator/setter
      #(let [^MultipleSelectionModel model (get-model %1)]
         (.clearSelection model)
         (when-not (empty? %2)
           (.selectIndices model
                           (first %2)
                           (into-array Integer/TYPE (rest %2))))))
    lifecycle/scalar))

(defn selected-items-prop [get-model items-lifecycle]
  (prop/make
    (mutator/setter
      #(let [^MultipleSelectionModel model (get-model %1)]
         (.clearSelection model)
         (doseq [item %2]
           (.select model item))))
    items-lifecycle))

(defn on-selected-indices-changed-prop [get-model]
  (prop/make
    (mutator/list-change-listener
      #(.getSelectedIndices ^MultipleSelectionModel (get-model %)))
    lifecycle/list-change-listener))

(defn on-selected-items-changed-prop [get-model]
  (prop/make
    (mutator/list-change-listener
      #(.getSelectedItems ^MultipleSelectionModel (get-model %)))
    lifecycle/list-change-listener))

(defn selection-mode-prop [get-model default]
  (prop/make
    (mutator/setter #(.setSelectionMode ^MultipleSelectionModel (get-model %1) %2))
    lifecycle/scalar
    :coerce (coerce/enum SelectionMode)
    :default default))

(defn- lift-ext-props [{:keys [desc props]} keys]
  {:props (select-keys props keys)
   :desc {:props (apply dissoc props keys)
          :desc desc}})

(defn- add-ext-flat-props [ext-with-props props-config]
  (let [keys (keys props-config)]
    (-> ext-with-props
        (lifecycle/make-ext-with-props props-config)
        (lifecycle/wrap-map-desc lift-ext-props keys))))

(defn make-with-props
  ([lifecycle get-model items-lifecycle default-mode]
   (make-with-props lifecycle get-model lifecycle/scalar items-lifecycle default-mode))
  ([lifecycle get-model item-lifecycle items-lifecycle default-mode]
   (-> lifecycle
       (ext.selection-model/make-with-props get-model item-lifecycle)
       (add-ext-flat-props
         {:selection-mode (selection-mode-prop get-model default-mode)})
       (add-ext-flat-props
         {:selected-indices (selected-indices-prop get-model)
          :selected-items (selected-items-prop get-model items-lifecycle)
          :on-selected-indices-changed (on-selected-indices-changed-prop get-model)
          :on-selected-items-changed (on-selected-items-changed-prop get-model)})
       (lifecycle/annotate 'cljfx.api/make-ext-with-props))))
