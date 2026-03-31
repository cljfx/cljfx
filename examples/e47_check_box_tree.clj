(ns e47-check-box-tree
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.paint Color]))

(set! *warn-on-reflection* true)

(defn javafx-color->web [^Color color]
  (let [r (int (* (.getRed color) 255))
        g (int (* (.getGreen color) 255))
        b (int (* (.getBlue color) 255))
        a (int (* (.getOpacity color) 255))]
    (format "#%02x%02x%02x%02x" r g b a)))

(defn- view [{:keys [state swap-state]}]
  {:fx/type :grid-pane
   :padding 20
   :hgap 10
   :vgap 10
   :children
   [{:fx/type :label
     :grid-pane/column 0
     :grid-pane/row 0
     :text "TreeView"}
    {:fx/type :label
     :grid-pane/column 1
     :grid-pane/row 0
     :text "Selection"}
    {:fx/type :tree-view
     :grid-pane/column 0
     :grid-pane/row 1
     :min-width 500
     :min-height 300
     :cell-factory
     {:fx/cell-type :check-box-tree-cell
      :describe
      (fn [x]
        {:text (str "«" (pr-str x) "»")
         :graphic
         {:fx/type :v-box
          :padding {:left 5}
          :alignment :center
          :children
          [{:fx/type :region
            :min-width 10
            :min-height 10
            :style {:-fx-background-color (javafx-color->web
                                            (try (Color/valueOf (str x))
                                                 (catch Throwable _ Color/GRAY)))}}]}})}
     :root ((fn ->check-box-tree-item [x]
              {:fx/type :check-box-tree-item
               :value x
               :expanded true
               :children (if (and (seqable? x) (not (string? x)))
                           (mapv ->check-box-tree-item x)
                           [])
               :selected (contains? state x)
               :on-selected-changed #(swap-state (if % conj disj) x)})
            {:a 1
             "green" [1 2 3 4 "red"]
             "blue" #{"foo" :a}
             :nses (all-ns)})}
    {:fx/type :list-view
     :grid-pane/column 1
     :grid-pane/row 1
     :min-width 500
     :items (vec (sort-by str state))}]})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene
     {:fx/type :scene
      :root
      {:fx/type fx/ext-state
       :initial-state #{1}
       :desc {:fx/type view}}}}))
