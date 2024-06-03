(ns e43-nested-local-state
  (:require [cljfx.api :as fx]))

(defn- text-view [{:keys [readonly swap-readonly] text :state swap-text :swap-state}]
  {:fx/type :v-box
   :padding 20
   :spacing 10
   :children [{:fx/type :check-box
               :text "Read-only"
               :selected readonly
               :on-selected-changed #(swap-readonly (constantly %))}
              {:fx/type :text-field
               :editable (not readonly)
               :text text
               :on-text-changed #(swap-text (constantly %))}]})

(defn- parent-view [{readonly :state swap-readonly :swap-state}]
  {:fx/type fx/ext-local-state
   :initial-state "my text"
   :desc {:fx/type text-view
          :readonly readonly
          :swap-readonly swap-readonly}})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type fx/ext-local-state
                    :initial-state true
                    :desc {:fx/type parent-view}}}}))

;; todo tests for ext-watcher, ext-local-state