(ns e27-selection-models
  (:require [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]
            [cljfx.ext.tab-pane :as fx.ext.tab-pane]
            [cljfx.ext.table-view :as fx.ext.table-view]
            [cljfx.ext.tree-view :as fx.ext.tree-view])
  (:import [javafx.scene.control Tab TreeItem]))

(set! *warn-on-reflection* true)

(defn init-state []
  {:selection ["/etc"
               "/users/vlaaad/.clojure"]
   :tree {"dev" {}
          "etc" {}
          "users" {"vlaaad" {".clojure" {"deps.edn" {}}}}
          "usr" {"bin" {}
                 "lib" {}}}})

(declare *state renderer)

; clean up renderer on file reload
(when (and (.hasRoot #'*state)
           (.hasRoot #'renderer))
  (fx/unmount-renderer *state renderer)
  (reset! *state (init-state)))

(defonce *state
  (atom (init-state)))

(defn list-view [{:keys [items selection selection-mode]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props (case selection-mode
            :multiple {:selection-mode :multiple
                       :selected-items selection
                       :on-selected-items-changed {:event/type ::select-multiple}}
            :single (cond-> {:selection-mode :single
                             :on-selected-item-changed {:event/type ::select-single}}
                      (seq selection)
                      (assoc :selected-item (-> selection sort first))))
   :desc {:fx/type :list-view
          :cell-factory {:fx/cell-type :list-cell
                         :describe (fn [path]
                                     {:text path})}
          :items items}})

(defn- let-refs [refs desc]
  {:fx/type fx/ext-let-refs
   :refs refs
   :desc desc})

(defn- get-ref [ref]
  {:fx/type fx/ext-get-ref
   :ref ref})

(defn- with-tab-selection-props [props desc]
  {:fx/type fx.ext.tab-pane/with-selection-props
   :props props
   :desc desc})

; Programatically setting tabs is somewhat buggy in JavaFX, so for
; demonstration purposes :selection-capabilities controls the selection model:
; - #{:read} the tab changes just based on the current state
; - #{:write} the tab changes just based the user selection (eg. clicking)
; - #{:read :write} both of the above (quite buggy, multiple tabs seem selected
;   and the content of tabs blend together)

(defn tab-pane [{:keys [items selection selection-capabilities]}]
  {:pre [(set? selection-capabilities)]}
  (let [selected-tab-id (-> selection sort first)
        _ (assert selected-tab-id)]
    (let-refs (into {}
                    (map (fn [item]
                           {:pre [(string? item)]}
                           [item
                            (merge
                              {:fx/type :tab
                               :graphic {:fx/type :label
                                         :text item}
                               :id item
                               :closable false}
                              (cond-> {}
                                ; buggy for :read'ing tabs
                                (:write selection-capabilities)
                                (assoc :content {:fx/type :label
                                                 :text item})

                                (not (:write selection-capabilities))
                                (assoc :disable (if selected-tab-id
                                                  (not= item selected-tab-id)
                                                  true))))]))
                    items)
      (with-tab-selection-props
        (cond-> {}
          (:read selection-capabilities) (assoc :selected-item (get-ref selected-tab-id))
          (:write selection-capabilities) (assoc :on-selected-item-changed {:event/type ::select-tab}))
        {:fx/type :tab-pane
         :tabs (map #(-> (get-ref %)
                         (assoc :fx/id %))
                    items)}))))

(defn table-view [{:keys [path->value selection selection-mode]}]
  {:fx/type fx.ext.table-view/with-selection-props
   :props (case selection-mode
            :multiple {:selection-mode :multiple
                       :selected-items selection
                       :on-selected-items-changed {:event/type ::select-multiple}}
            :single (cond-> {:selection-mode :single
                             :on-selected-item-changed {:event/type ::select-single}}
                      (seq selection)
                      (assoc :selected-item (-> selection sort first))))
   :desc {:fx/type :table-view
          :columns [{:fx/type :table-column
                     :text "path"
                     :cell-value-factory identity
                     :cell-factory {:fx/cell-type :table-cell
                                    :describe (fn [path]
                                                {:text path})}}
                    {:fx/type :table-column
                     :text "elements"
                     :cell-value-factory identity
                     :cell-factory {:fx/cell-type :table-cell
                                    :describe (fn [path]
                                                {:text (str (count (path->value path)))})}}]
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

(defn tree-view [{:keys [tree selection selection-mode]}]
  (make-tree-item-refs
    tree
    {:fx/type fx.ext.tree-view/with-selection-props
     :props (case selection-mode
              :multiple {:selection-mode :multiple
                         :selected-items (for [x selection]
                                           {:fx/type fx/ext-get-ref
                                            :ref x})
                         :on-selected-items-changed {:event/type ::select-tree-items}}
              :single (cond-> {:selection-mode :single
                               :on-selected-item-changed {:event/type ::select-tree-item}}
                        (seq selection)
                        (assoc :selected-item {:fx/type fx/ext-get-ref
                                               :ref (-> selection sort first)})))
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

(defn add-header [title desc]
  {:fx/type :v-box
   :spacing 10
   :children [{:fx/type :label
               :text title}
              desc]})

(defn on-grid [{:keys [rows columns descs]}]
  {:pre [(vector? descs)
         (<= (count descs)
             (* rows columns))]}
  {:fx/type :grid-pane
   :hgap 20
   :vgap 20
   :padding 30
   :row-constraints (repeat rows {:fx/type :row-constraints
                                  :percent-height (/ 100 rows)})
   :column-constraints (repeat columns {:fx/type :column-constraints
                                        :percent-width (/ 100 columns)})
   :children (for [row (range rows)
                   col (range columns)
                   :let [loc (+ (* row rows) col)]
                   :when (< loc (count descs))]
               (-> (nth descs loc)
                   (assoc :grid-pane/row row
                          :grid-pane/column col)))})

(defn view [{{:keys [tree selection]} :state}]
  (let [path->value (make-path->value tree)]
    {:fx/type :stage
     :showing true
     :width 960
     :scene {:fx/type :scene
             :root {:fx/type :scroll-pane
                    :fit-to-width true
                    :fit-to-height true
                    :content
                    {:fx/type on-grid
                     :rows 3
                     :columns 3
                     :descs [(add-header
                               ":list-view :multiple"
                               {:fx/type list-view
                                :items (keys path->value)
                                :selection-mode :multiple
                                :selection selection})
                             (add-header
                               ":list-view :single"
                               {:fx/type list-view
                                :items (keys path->value)
                                :selection-mode :single
                                :selection selection})
                             (add-header
                               ":table-view :multiple"
                               {:fx/type table-view
                                :path->value path->value
                                :selection-mode :multiple
                                :selection selection})
                             (add-header
                               ":table-view :single"
                               {:fx/type table-view
                                :path->value path->value
                                :selection-mode :single
                                :selection selection})
                             (add-header
                               ":tree-view :multiple"
                               {:fx/type tree-view
                                :tree tree
                                :selection-mode :multiple
                                :selection selection})
                             (add-header
                               ":tree-view :single"
                               {:fx/type tree-view
                                :tree tree
                                :selection-mode :single
                                :selection selection})
                             (add-header
                               ":tab-pane (opens current selection)"
                               {:fx/type tab-pane
                                :items (keys path->value)
                                :selection-capabilities #{:read}
                                :selection selection})
                             (add-header
                               ":tab-pane (only changes on click)"
                               {:fx/type tab-pane
                                :items (keys path->value)
                                :selection-capabilities #{:write}
                                :selection selection})
                             ; buggy, content from different tabs
                             ; blend into eachother
                             #_
                             (add-header
                               ":tab-pane (read+write)"
                               {:fx/type tab-pane
                                :items (keys path->value)
                                :selection-capabilities #{:read :write}
                                :selection selection})]}}}}))

(defn tree-item-value [^TreeItem x]
  (.getValue x))

(defn tab-id [^Tab x]
  {:post [(string? %)]}
  (.getId x))

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
              (swap! *state assoc :selection [(-> % :fx/event tree-item-value)])

              ::select-multiple
              (swap! *state assoc :selection (:fx/event %))
              ::select-single
              (swap! *state assoc :selection [(:fx/event %)])

              ::select-tab
              (swap! *state assoc :selection [(-> % :fx/event tab-id)]))}))

(fx/mount-renderer *state renderer)
