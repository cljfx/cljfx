(ns e13-re-frame-like-state
  (:require [cljfx.api :as fx]))

(def *state
  (atom
    (fx/create-context
      {:id->potion {0 {:id 0
                       :name "Antidote"
                       :ingredient-ids [0 1]}
                    1 {:id 1
                       :name "Explosive cocktail"
                       :ingredient-ids [2 3 4]}
                    2 {:id 2
                       :name "Petrificoffee"
                       :ingredient-ids [1 5]}
                    3 {:id 3
                       :name "Viscositea"
                       :ingredient-ids [0 4]}}
       :id->ingredient {0 {:id 0
                           :name "Applebloom"}
                        1 {:id 1
                           :name "Dragonwort"}
                        2 {:id 2
                           :name "Ruby"}
                        3 {:id 3
                           :name "Fireberries"}
                        4 {:id 4
                           :name "Sulfur"}
                        5 {:id 5
                           :name "Web"}}})))

(defn sub-potion-ids [context]
  (sort (keys (fx/sub context :id->potion))))

(defn sub-ingredient-ids [context]
  (sort (keys (fx/sub context :id->ingredient))))

(defn sub-id->potion [context id]
  (get (fx/sub context :id->potion) id))

(defn sub-id->ingredient [context id]
  (get (fx/sub context :id->ingredient) id))

(def sub-ingredient-id->potion-ids-map
  ^:fx/cached
  (fn [context]
    (->> (fx/sub context :id->potion)
         vals
         (mapcat (fn [potion]
                   (map (fn [ingredient-id]
                          [(:id potion) ingredient-id])
                        (:ingredient-ids potion))))
         (group-by second)
         (map (juxt key
                    #(->> % val (map first) sort)))
         (into {}))))

(defn sub-ingredient-id->potion-ids [context id]
  (get (fx/sub context sub-ingredient-id->potion-ids-map) id))

(defn section-title [{:keys [text]}]
  {:fx/type :label
   :style {:-fx-font [:bold 20 :sans-serif]}
   :text text})

(defn item-title [{:keys [text]}]
  {:fx/type :label
   :style {:-fx-font [15 :sans-serif]}
   :text text})

(defn small-label [{:keys [text]}]
  {:fx/type :label
   :style {:-fx-font [12 :sans-serif]
           :-fx-text-fill :grey}
   :text text})

(defn badge [{:keys [text]}]
  {:fx/type :label
   :style {:-fx-font [12 :sans-serif]
           :-fx-text-fill :grey
           :-fx-border-width 1
           :-fx-border-style :solid
           :-fx-border-color :lightgray
           :-fx-border-radius 4
           :-fx-padding [0 2 0 2]}
   :text text})

(defn ingredient-badge [{:keys [fx/context id]}]
  {:fx/type badge
   :text (:name (fx/sub context sub-id->ingredient id))})

(defn potion-view [{:keys [fx/context id]}]
  (let [potion (fx/sub context sub-id->potion id)]
    {:fx/type :v-box
     :children [{:fx/type item-title
                 :text (:name potion)}
                {:fx/type :h-box
                 :spacing 2
                 :children (concat
                             [{:fx/type small-label
                               :text "needs"}]
                             (for [id (:ingredient-ids potion)]
                               {:fx/type ingredient-badge
                                :id id}))}]}))

(defn potion-list [{:keys [fx/context]}]
  {:fx/type :v-box
   :padding 10
   :spacing 10
   :children [{:fx/type section-title
               :text "Potions"}
              {:fx/type :v-box
               :spacing 5
               :children (for [id (fx/sub context sub-potion-ids)]
                           {:fx/type potion-view
                            :id id})}]})

(defn potion-badge [{:keys [fx/context id]}]
  {:fx/type badge
   :text (:name (fx/sub context sub-id->potion id))})

(defn ingredient-view [{:keys [fx/context id]}]
  (let [ingredient (fx/sub context sub-id->ingredient id)]
    {:fx/type :v-box
     :children [{:fx/type item-title
                 :text (:name ingredient)}
                {:fx/type :h-box
                 :spacing 2
                 :children (concat
                             [{:fx/type small-label
                               :text "used in"}]
                             (for [id (fx/sub context sub-ingredient-id->potion-ids id)]
                               {:fx/type potion-badge
                                :id id}))}]}))

(defn ingredient-list [{:keys [fx/context]}]
  {:fx/type :v-box
   :padding 10
   :spacing 10
   :children [{:fx/type section-title
               :text "Ingredients"}
              {:fx/type :v-box
               :spacing 5
               :children (for [id (fx/sub context sub-ingredient-ids)]
                           {:fx/type ingredient-view
                            :id id})}]})

(defn root-view [_]
  {:fx/type :stage
   :title "Book of potions"
   :showing true
   :width 600
   :height 400
   :scene {:fx/type :scene
           :root {:fx/type :split-pane
                  :divider-positions [0.5]
                  :items [{:fx/type potion-list}
                          {:fx/type ingredient-list}]}}})

(def app
  (fx/create-app
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_] {:fx/type root-view})))
    :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(fx/mount-app *state app)
