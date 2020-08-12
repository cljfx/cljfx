(ns e13-re-frame-like-state
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]))

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
                           :name "Web"}}}
      cache/lru-cache-factory)))

(defn sub-potion-ids [context]
  (sort (keys (fx/sub-val context :id->potion))))

(defn sub-ingredient-ids [context]
  (sort (keys (fx/sub-val context :id->ingredient))))

(defmulti event-handler :event/type)

(defmethod event-handler ::remove-ingredient [{:keys [potion-id ingredient-id]}]
  (swap! *state
         fx/swap-context
         update-in
         [:id->potion potion-id :ingredient-ids]
         #(vec (remove #{ingredient-id} %))))

(defn close-icon [{:keys [on-remove]}]
  {:fx/type :stack-pane
   :on-mouse-clicked on-remove
   :children
   [{:fx/type :svg-path
     :fill :gray
     :scale-x 0.75
     :scale-y 0.75
     :content (str "M15.898,4.045c-0.271-0.272-0.713-0.272-0.986,0l-4.71,4.711L5.493,"
                   "4.045c-0.272-0.272-0.714-0.272-0.986,0s-0.272,0.714,0,0.986l4.709,"
                   "4.711l-4.71,4.711c-0.272,0.271-0.272,0.713,0,0.986c0.136,0.136,0.314,"
                   "0.203,0.492,0.203c0.179,0,0.357-0.067,0.493-0.203l4.711-4.711l4.71,"
                   "4.711c0.137,0.136,0.314,0.203,0.494,0.203c0.178,0,0.355-0.067,"
                   "0.492-0.203c0.273-0.273,0.273-0.715,0-0.986l-4.711-4.711l4.711-4."
                   "711C16.172,4.759,16.172,4.317,15.898,4.045z")}]})

(defn sub-ingredient-id->potion-ids-map [context]
  (->> (fx/sub-val context :id->potion)
       vals
       (mapcat (fn [potion]
                 (map (fn [ingredient-id]
                        [(:id potion) ingredient-id])
                      (:ingredient-ids potion))))
       (group-by second)
       (map (juxt key
                  #(->> % val (map first) sort)))
       (into {})))

(defn sub-ingredient-id->potion-ids [context id]
  (get (fx/sub-ctx context sub-ingredient-id->potion-ids-map) id))

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

(defn badge [{:keys [text on-remove]}]
  {:fx/type :h-box
   :alignment :center
   :spacing 2
   :style {:-fx-font [12 :sans-serif]
           :-fx-text-fill :grey
           :-fx-border-width 1
           :-fx-border-style :solid
           :-fx-border-color :lightgray
           :-fx-border-radius 4
           :-fx-padding [0 2 0 2]}
   :children [{:fx/type :label :text text}
              {:fx/type close-icon
               :on-remove on-remove}]})

(defn ingredient-badge [{:keys [fx/context id potion-id]}]
  {:fx/type badge
   :text (fx/sub-val context get-in [:id->ingredient id :name])
   :on-remove {:event/type ::remove-ingredient
               :potion-id potion-id
               :ingredient-id id}})

(defn potion-view [{:keys [fx/context id]}]
  (let [potion (fx/sub-val context get-in [:id->potion id])]
    {:fx/type :v-box
     :children [{:fx/type item-title
                 :text (:name potion)}
                {:fx/type :h-box
                 :spacing 2
                 :children (concat
                             [{:fx/type small-label
                               :text "needs"}]
                             (for [ingredient-id (:ingredient-ids potion)]
                               {:fx/type ingredient-badge
                                :potion-id id
                                :id ingredient-id}))}]}))

(defn potion-list [{:keys [fx/context]}]
  {:fx/type :v-box
   :padding 10
   :spacing 10
   :children [{:fx/type section-title
               :text "Potions"}
              {:fx/type :v-box
               :spacing 5
               :children (for [id (fx/sub-ctx context sub-potion-ids)]
                           {:fx/type potion-view
                            :id id})}]})

(defn potion-badge [{:keys [fx/context id ingredient-id]}]
  {:fx/type badge
   :text (fx/sub-val context get-in [:id->potion id :name])
   :on-remove {:event/type ::remove-ingredient
               :potion-id id
               :ingredient-id ingredient-id}})

(defn ingredient-view [{:keys [fx/context id]}]
  (let [potion-ids (fx/sub-ctx context sub-ingredient-id->potion-ids id)]
    {:fx/type :v-box
     :children [{:fx/type item-title
                 :text (fx/sub-val context get-in [:id->ingredient id :name])}
                {:fx/type :h-box
                 :spacing 2
                 :children (if (empty? potion-ids)
                             [{:fx/type small-label
                               :text "unused"}]
                             (concat
                               [{:fx/type small-label
                                 :text "used in"}]
                               (for [potion-id potion-ids]
                                 {:fx/type potion-badge
                                  :ingredient-id id
                                  :id potion-id})))}]}))

(defn ingredient-list [{:keys [fx/context]}]
  {:fx/type :v-box
   :padding 10
   :spacing 10
   :children [{:fx/type section-title
               :text "Ingredients"}
              {:fx/type :v-box
               :spacing 5
               :children (for [id (fx/sub-ctx context sub-ingredient-ids)]
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

(def renderer
  (fx/create-renderer
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_] {:fx/type root-view})))
    :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))
           :fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
