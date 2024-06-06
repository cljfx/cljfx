(ns e44-nested-form-view
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.input KeyCode KeyEvent]))

;; This example show using nested local states for stateful components defined in
;; a declarative way.
;; This is a form for editing a data structure with 2 text field and one checkbox.
;; Text field edits are applied on commit (e.g. enter or focus loss), and reset on escape.
;; The whole form also allows resetting and committing. Local state synchronisation
;; is fully declarative.

(defn text-field-impl-view [{:keys [state swap-state swap-text]}]
  (let [{:keys [edit text]} state]
    {:fx/type :text-field
     :style (if (= edit text) {} {:-fx-text-fill "#07a"}) ;; highlight uncommitted changes
     :text edit
     :on-key-pressed (fn [^KeyEvent e]
                       (when (= KeyCode/ESCAPE (.getCode e))
                         (swap-state #(assoc % :edit (:text %)))))
     :on-text-changed #(swap-state assoc :edit %)
     :on-focused-changed (fn [focused]
                           (when (and (not focused)
                                      (not= edit text))
                             (swap-text (constantly edit))))
     :on-action (fn [_] (swap-text (constantly edit)))}))

(defn text-field-view [{:keys [state swap-state]}]
  {:fx/type fx/ext-local-state
   :initial-state {:text state :edit state}
   :desc {:fx/type text-field-impl-view
          :swap-text swap-state}})

(defn check-box-view [{:keys [state swap-state]}]
  {:fx/type :check-box
   :selected state
   :on-selected-changed #(swap-state (constantly %))})

(declare schema-view)

(defn record-view [{:keys [entries state swap-state]}]
  {:fx/type :grid-pane
   :hgap 5
   :vgap 5
   :children (into []
                   (comp
                     (map-indexed
                       (fn [row [k schema]]
                         [{:fx/type :label
                           :grid-pane/row row
                           :grid-pane/column 0
                           :text (name k)}
                          {:fx/type schema-view
                           :grid-pane/row row
                           :grid-pane/column 1
                           :schema schema
                           :state (get state k)
                           :swap-state (partial swap-state update k)}]))
                     cat)
                   entries)})

(defn schema-view [{:keys [schema state swap-state]}]
  (assoc schema
    :fx/type (case (:type schema)
               :string text-field-view
               :boolean check-box-view
               :record record-view)
    :state state
    :swap-state swap-state))

(defn committable-schema-impl-view [{:keys [state swap-state initial-state swap-parent-state schema]}]
  (let [{:keys [current edit]} state]
    {:fx/type :v-box
     :spacing 5
     :children [{:fx/type schema-view
                 :schema schema
                 :state edit
                 :swap-state (partial swap-state update :edit)}
                {:fx/type :h-box
                 :alignment :center-right
                 :spacing 5
                 :children [{:fx/type :button
                             :disable (= current edit)
                             :text "Reset"
                             :on-action (fn [_]
                                          (swap-state assoc :edit initial-state))}
                            {:fx/type :button
                             :text "Commit"
                             :disable (= current edit)
                             :on-action (fn [_]
                                          (swap-parent-state (constantly edit)))}]}]}))

(defn committable-schema-view [{:keys [schema state swap-state]}]
  {:fx/type fx/ext-local-state
   :initial-state {:current state :edit state}
   :desc {:fx/type committable-schema-impl-view
          :swap-parent-state swap-state
          :schema schema
          :initial-state state}})


(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :padding 10
                    :children [{:fx/type fx/ext-local-state
                                :initial-state {:first-name "Vlop"
                                                :last-name "Prop"
                                                :cool true}
                                :desc {:fx/type committable-schema-view
                                       :schema {:type :record
                                                :entries [[:first-name {:type :string}]
                                                          [:last-name {:type :string}]
                                                          [:cool {:type :boolean}]]}}}]}}}))