(ns e16-cell-factories
  (:require [cljfx.api :as fx]))

(def list-view
  {:fx/type :list-view
   :cell-factory {:fx/cell-type :list-cell
                  :describe (fn [i]
                              (let [color (format "#%03x" i)]
                                {:style {:-fx-background-color color}
                                 :text color}))}
   :items (range 16r1000)})

(def table-view
  {:fx/type :table-view
   :row-factory {:fx/cell-type :table-row
                 :describe (fn [x]
                             {:style {:-fx-border-color x}})}
   :columns [{:fx/type :table-column
              :text "pr-str"
              :cell-value-factory identity
              :cell-factory {:fx/cell-type :table-cell
                             :describe (fn [x]
                                         {:text (pr-str x)})}}
             {:fx/type :table-column
              :text "bg color"
              :cell-value-factory identity
              :cell-factory {:fx/cell-type :table-cell
                             :describe (fn [i]
                                         {:style {:-fx-background-color i}})}}]
   :items [:red :green :blue "#ccc4" "#ccc4"]})

(def combo-box
  {:fx/type :combo-box
   :button-cell (fn [user] {:text (:name user)})
   :cell-factory {:fx/cell-type :list-cell
                  :describe (fn [user] {:text (:name user)})}
   :items [{:name "Fred"}
           {:name "Rick"}]})

(defn- ->tree-item [x]
  (cond
    (string? x) {:fx/type :tree-item :value x}
    (seqable? x) {:fx/type :tree-item :value x :children (map ->tree-item x)}
    :else {:fx/type :tree-item :value x}))

(def tree-table-view
  {:fx/type :tree-table-view
   :row-factory {:fx/cell-type :tree-table-row
                 :describe (fn [x]
                             {:style {:-fx-background-color (cond
                                                              (number? x) "#99f"
                                                              (string? x) "#cfa"
                                                              (map? x) "fda"
                                                              (set? x) :pink
                                                              (coll? x) "#faa"
                                                              (keyword? x) "eaf"
                                                              :else "#adf")}})}
   :columns [{:fx/type :tree-table-column
              :text "pr-str"
              :max-width 960/2
              :cell-value-factory identity
              :cell-factory {:fx/cell-type :tree-table-cell
                             :describe (fn [x]
                                         {:text (pr-str x)})}}
             {:fx/type :tree-table-column
              :text "str"
              :max-width 960/2
              :cell-value-factory identity
              :cell-factory {:fx/cell-type :tree-table-cell
                             :describe (fn [x]
                                         {:text (str x)})}}]
   :root (->tree-item
           {:set #{:a :b :c}
            :scalars ["string" false 1 1M 1/2 1.0 'symbol :keyword]
            :map {:a 1}
            :vec [1 2 3]
            :list '(1 2 3)
            :range (range 4)})})

(def tree-view
  {:fx/type :tree-view
   :cell-factory {:fx/cell-type :tree-cell
                  :describe (fn [x]
                              {:text (str x)})}
   :root (->tree-item table-view)})

(def date-picker
  {:fx/type :date-picker
   :day-cell-factory {:fx/cell-type :date-cell
                      :describe (fn [^java.time.LocalDate x]
                                  ;; Christmas theme ^_^
                                  {:text-fill (if (even? (.getDayOfYear x)) :red :green)})}})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :title "Cell factory examples"
     :scene {:fx/type :scene
             :root {:fx/type :tab-pane
                    :pref-width 960
                    :pref-height 540
                    :tabs [{:fx/type :tab
                            :text "Table View"
                            :closable false
                            :content table-view}
                           {:fx/type :tab
                            :text "List View"
                            :closable false
                            :content list-view}
                           {:fx/type :tab
                            :text "Combo Box"
                            :closable false
                            :content combo-box}
                           {:fx/type :tab
                            :text "Tree Table View"
                            :closable false
                            :content tree-table-view}
                           {:fx/type :tab
                            :text "Tree View"
                            :closable false
                            :content tree-view}
                           {:fx/type :tab
                            :text "Date Picker"
                            :closable false
                            :content date-picker}]}}}))
