(ns e15-task-tracker
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]))

(def *state
  (atom
    (fx/create-context
      {:tasks {0 {:id 0
                  :title "Fix NPE on logout during full moon"
                  :state :in-progress
                  :assignee 0}
               1 {:id 1
                  :title "Allow changing task state"
                  :state :done
                  :assignee 1}
               2 {:id 2
                  :title "Allow creating tasks"
                  :state :todo}}
       :users {0 {:id 0
                  :name "Fred"}
               1 {:id 1
                  :name "Alice"}
               2 {:id 2
                  :name "Rick"}}}
      cache/lru-cache-factory)))

;; subscription functions

(defn sub-make-task-state->task-ids [context]
  (->> (fx/sub-val context :tasks)
       vals
       (group-by :state)
       (map (juxt key #(->> % val (map :id) (sort-by -))))
       (into {})))

(defn sub-task-state->task-ids [context task-state]
  (get (fx/sub-ctx context sub-make-task-state->task-ids) task-state))

(defn sub-task-count [context task-state]
  (count (fx/sub-ctx context sub-task-state->task-ids task-state)))

(defn sub-task-by-id [context i]
  (fx/sub-val context get-in [:tasks i]))

(defn sub-user-by-id [context i]
  (fx/sub-val context get-in [:users i]))

(defn sub-all-users [context]
  (->> (fx/sub-val context :users)
       vals
       (sort-by :id)))

;; event handling

(defmulti event-handler :event/type)

(defmethod event-handler ::assign-user [{:keys [task-id fx/event]}]
  (swap! *state fx/swap-context assoc-in [:tasks task-id :assignee] (:id event)))

(defmethod event-handler ::set-state [{:keys [task-id fx/event]}]
  (swap! *state fx/swap-context assoc-in [:tasks task-id :state] event))

(defmethod event-handler :default [x] (prn x))

;; views

(defn column-task-count [{:keys [fx/context task-state]}]
  {:fx/type :label
   :text (str (fx/sub-ctx context sub-task-count task-state))})

(defn assignee-view [{:keys [fx/context task-id id]}]
  (let [user (fx/sub-ctx context sub-user-by-id id)]
    {:fx/type :h-box
     :alignment :center
     :spacing 5
     :children [{:fx/type :label
                 :text "Assignee:"}
                {:fx/type :combo-box
                 :button-cell (fn [user] {:text (:name user)})
                 :cell-factory {:fx/cell-type :list-cell
                                :describe (fn [user] {:text (:name user)})}
                 :value user
                 :on-value-changed {:event/type ::assign-user :task-id task-id}
                 :items (fx/sub-ctx context sub-all-users)}]}))

(defn task-state-view [{:keys [fx/context id]}]
  (let [task (fx/sub-ctx context sub-task-by-id id)]
    {:fx/type :h-box
     :alignment :center
     :spacing 5
     :children [{:fx/type :label
                 :text "State:"}
                {:fx/type :combo-box
                 :value (:state task)
                 :on-value-changed {:event/type ::set-state :task-id id}
                 :items [:todo :in-progress :done]}]}))

(defn task-view [{:keys [fx/context id]}]
  (let [task (fx/sub-ctx context sub-task-by-id id)]
    {:fx/type :v-box
     :style {:-fx-background-color "#eee"
             :-fx-background-radius 2
             :-fx-padding 5}
     :effect {:fx/type :drop-shadow
              :offset-y 2
              :color "#0005"}
     :spacing 5
     :children [{:fx/type :label
                 :style {:-fx-font [16 :sans-serif]}
                 :text (:title task)}
                {:fx/type :flow-pane
                 :hgap 10
                 :vgap 5
                 :children [{:fx/type assignee-view
                             :task-id id
                             :id (:assignee task)}
                            {:fx/type task-state-view
                             :id id}]}]}))

(defn column-tasks [{:keys [fx/context task-state]}]
  {:fx/type :v-box
   :spacing 10
   :children (for [task-id (fx/sub-ctx context sub-task-state->task-ids task-state)]
               {:fx/type task-view
                :fx/key task-id
                :id task-id})})

(defn column [{:keys [task-state]}]
  {:fx/type :v-box
   :style {:-fx-background-color "#ddd"
           :-fx-background-radius 2}
   :effect {:fx/type :drop-shadow
            :radius 8
            :offset-y 2
            :color "#0004"}
   :padding 5
   :spacing 15
   :children [{:fx/type :h-box
               :children [{:fx/type :label
                           :h-box/hgrow :always
                           :max-width Double/MAX_VALUE
                           :text (str task-state)}
                          {:fx/type column-task-count
                           :task-state task-state}]}
              {:fx/type column-tasks
               :task-state task-state}]})

(defn root [_]
  {:fx/type :stage
   :showing true
   :width 1024
   :height 540
   :scene {:fx/type :scene
           :root {:fx/type :grid-pane
                  :padding 10
                  :column-constraints [{:fx/type :column-constraints
                                        :percent-width 100/3}
                                       {:fx/type :column-constraints
                                        :percent-width 100/3}
                                       {:fx/type :column-constraints
                                        :percent-width 100/3}]
                  :children [{:fx/type column
                              :grid-pane/vgrow :always
                              :grid-pane/column 0
                              :grid-pane/margin 5
                              :task-state :todo}
                             {:fx/type column
                              :grid-pane/vgrow :always
                              :grid-pane/column 1
                              :grid-pane/margin 5
                              :task-state :in-progress}
                             {:fx/type column
                              :grid-pane/vgrow :always
                              :grid-pane/column 2
                              :grid-pane/margin 5
                              :task-state :done}]}}})

(def renderer
  (fx/create-renderer
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_] {:fx/type root})))
    :opts {:fx.opt/map-event-handler event-handler
           :fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(fx/mount-renderer *state renderer)
