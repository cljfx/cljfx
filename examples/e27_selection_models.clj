(ns e27-selection-models
  (:require [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]
            [cljfx.ext.table-view :as fx.ext.table-view]
            [cljfx.ext.tree-view :as fx.ext.tree-view])
  (:import [javafx.scene.control TreeItem]))

(def *state
  (atom {:selection ["/etc"
                     "/users/vlaaad/.clojure"]
         :tree {"dev" {}
                "etc" {}
                "users" {"vlaaad" {".clojure" {"deps.edn" {}}}}
                "usr" {"bin" {}
                       "lib" {}}}}))

(defn list-view [{:keys [items selection]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props {:selection-mode :multiple
           :selected-items selection
           :on-selected-items-changed {:event/type ::select}}
   :desc {:fx/type :list-view
          :cell-factory (fn [path]
                          {:text path})
          :items items}})

(defn table-view [{:keys [path->value selection]}]
  {:fx/type fx.ext.table-view/with-selection-props
   :props {:selection-mode :multiple
           :selected-items selection
           :on-selected-items-changed {:event/type ::select}}
   :desc {:fx/type :table-view
          :columns [{:fx/type :table-column
                     :text "path"
                     :cell-factory identity
                     :cell-value-factory (fn [path]
                                           {:text path})}
                    {:fx/type :table-column
                     :text "elements"
                     :cell-factory identity
                     :cell-value-factory (fn [path]
                                           {:text (str (count (path->value path)))})}]
          :items (keys path->value)}})

;; Situation with tree-view is complicated by the fact that it's selection model is
;; parameterized by TreeItems, and selection expects items. To workaround it we create
;; tree-item descriptions inside using `fx/ext-let-refs`, so we can refer to them later
;; with `fx/ext-get-ref` containing a "path", which is conveniently a value with use for
;; selection. Since defined tree-item descriptions can have children, which are also tree
;; items, we need to declare them first, in a separate `fx/ext-let-refs`, and then refer
;; to them in parent's `fx/ext-let-refs`, that makes code really hard to follow.

(defn children-refs [prefix m]
  (->> m
       keys
       (mapv (fn [x] {:fx/type fx/ext-get-ref
                      :ref (str prefix "/" x)}))))

(defn- make-tree-item-refs [tree desc]
  (letfn [(f [x prefix desc]
            (let [children (for [[k vs] x
                                 v vs]
                             [(str prefix "/" k) v])
                  new-desc {:fx/type fx/ext-let-refs
                            :refs (into {} (for [[k v] x
                                                 :let [path (str prefix "/" k)]]
                                             [path {:fx/type :tree-item
                                                    :value path
                                                    :children (children-refs path v)}]))
                            :desc desc}]

              (reduce (fn [desc [prefix e]]
                        (f (into {} [e]) prefix desc))
                      new-desc children)))]
    (f tree "" desc)))

(defn tree-view [{:keys [tree selection]}]
  (make-tree-item-refs
    tree
    {:fx/type fx.ext.tree-view/with-selection-props
     :props {:selection-mode :multiple
             :selected-items (for [x selection]
                               {:fx/type fx/ext-get-ref
                                :ref x})
             :on-selected-items-changed {:event/type ::select-tree-items}}
     :desc {:fx/type :tree-view
            :show-root false
            :root {:fx/type :tree-item
                   :children (children-refs "" tree)}}}))

; chooses the (lexicographically) smallest selection
(defn single-tree-view [{:keys [tree selection]}]
  (make-tree-item-refs
    tree
    {:fx/type fx.ext.tree-view/with-selection-props
     :props (merge
              {:on-selected-item-changed {:event/type ::select-tree-item}}
              (when (seq selection)
                {:selected-item {:fx/type fx/ext-get-ref
                                 :ref (-> selection sort first)}}))
     :desc {:fx/type :tree-view
            :show-root false
            :root {:fx/type :tree-item
                   :children (children-refs "" tree)}}}))

;; For list-view and table view we don't need such complicated setup, since their
;; selection is described by the same values they have in `:items`, so we just create a
;; map from paths to "directory" contents on these paths

(defn- make-path->value [tree]
  (letfn [(flatten-map [prefix x]
            (mapcat (fn [[k v]]
                      (let [path (str prefix "/" k)]
                        (cons [path v] (flatten-map path v))))
                    x))]
    (into (sorted-map)
          (flatten-map "" tree))))

(defn view [{{:keys [tree selection]} :state}]
  (let [path->value (make-path->value tree)]
    {:fx/type :stage
     :showing true
     :width 960
     :scene {:fx/type :scene
             :root {:fx/type :grid-pane
                    :column-constraints [{:fx/type :column-constraints
                                          :percent-width 100/4}
                                         {:fx/type :column-constraints
                                          :percent-width 100/4}
                                         {:fx/type :column-constraints
                                          :percent-width 100/4}
                                         {:fx/type :column-constraints
                                          :percent-width 100/4}]
                    :children [{:fx/type list-view
                                :grid-pane/column 0
                                :items (keys path->value)
                                :selection selection}
                               {:fx/type table-view
                                :grid-pane/column 1
                                :path->value path->value
                                :selection selection}
                               {:fx/type tree-view
                                :grid-pane/column 2
                                :tree tree
                                :selection selection}
                               {:fx/type single-tree-view
                                :grid-pane/column 3
                                :tree tree
                                :selection selection}]}}}))

(defn tree-item-value [^TreeItem x]
  (.getValue x))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type view
                                     :state state}))
    :opts {:fx.opt/map-event-handler
           #(case (:event/type %)
              ::select-tree-items
              (swap! *state assoc :selection (->> % :fx/event (mapv tree-item-value)))
              ::select-tree-item
              (swap! *state assoc :selection (->> % :fx/event tree-item-value vector))

              ::select
              (swap! *state assoc :selection (:fx/event %)))}))

(fx/mount-renderer *state renderer)
