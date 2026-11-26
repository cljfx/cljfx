(ns cljfx.root-scheduling-test
  (:require [cljfx.api :as fx]
            [cljfx.component :as component]
            [cljfx.lifecycle :as lifecycle]
            [clojure.test :refer [deftest is testing]])
  (:import [javafx.scene Scene]
           [javafx.scene.control Label ListCell ListView Pagination TextField]
           [javafx.scene.layout Pane Priority StackPane VBox]))

(defn flush-fx! [] @(fx/run-later))

;; Ref changes reconcile parents, preserve state, and coalesce across refs.
(deftest root-reconciliation-test
  (testing "Node/nil/type replacements preserve list constraints"
    (let [ref (atom :label)

          desc
          {:fx/type :v-box
           :children [{:fx/type fx/ext-watcher
                       :ref ref
                       :v-box/vgrow :always
                       :desc {:fx/type (fn conditional-child [{:keys [value]}]
                                         (case value
                                           :label {:fx/type :label :text "label"}
                                           :field {:fx/type :text-field :text "field"}
                                           nil))}}]}

          c @(fx/on-fx-thread (fx/create-component desc))
          ^VBox box (fx/instance c)]
      (try
        (doseq [[value klass] [[:none nil] [:field TextField] [:label Label] [:none nil]]]
          (reset! ref value)
          (flush-fx!)
          (is (= (if klass 1 0) (count (.getChildren box))))
          (when klass
            (is (instance? klass (first (.getChildren box))))
            (is (= Priority/ALWAYS (VBox/getVgrow (first (.getChildren box)))))))
        (finally @(fx/on-fx-thread (fx/delete-component c))))
      (is (= {} (.getWatches ref)))))
  (testing "Nil state output remains mounted and restores with its counter and updater"
    (let [update-state (atom nil)

          c
          @(fx/on-fx-thread
             (fx/create-component
               {:fx/type :v-box
                :children [{:fx/type fx/ext-state
                            :initial-state {:visible true :count 0}
                            :desc {:fx/type (fn stateful-child [{:keys [state swap-state]}]
                                              (reset! update-state swap-state)
                                              (when (:visible state)
                                                {:fx/type :label :text (str (:count state))}))}}]}))

          ^VBox box (fx/instance c)
          updater @update-state]
      (try
        (updater assoc :visible false :count 41)
        (flush-fx!)
        (is (zero? (count (.getChildren box))))
        (updater assoc :visible true)
        (flush-fx!)
        (is (= "41" (.getText ^Label (first (.getChildren box)))))
        (is (identical? updater @update-state))
        (finally @(fx/on-fx-thread (fx/delete-component c))))))
  (testing "A burst across independent watchers advances their common root once"
    (let [refs (mapv atom (range 40))
          passes (atom 0)

          tracker
          (reify lifecycle/Lifecycle
            (create [_ _ _] nil)
            (advance [_ _ _ _] (swap! passes inc) nil)
            (delete [_ _ _]))

          c
          @(fx/on-fx-thread
             (fx/create-component
               {:fx/type fx/ext-many
                :desc (into [{:fx/type tracker}]
                            (mapv (fn describe-watcher [ref]
                                    {:fx/type fx/ext-watcher
                                     :ref ref
                                     :key :user-data
                                     :desc {:fx/type :label :text "sibling"}})
                                  refs))}))]
      (try
        @(fx/on-fx-thread (dotimes [_ 10] (run! #(swap! % inc) refs)))
        (flush-fx!)
        (is (= 1 @passes))
        (finally @(fx/on-fx-thread (fx/delete-component c))))
      (is (zero? (transduce (map #(count (.getWatches %))) + refs))))))

;; Invalidation survives render-time changes, errors, and queued deletion.
(deftest root-scheduling-lifetime-test
  (testing "Updates requested during advancement are not lost, including ABA changes"
    (let [ref (atom 0)
          values (atom [])
          done (promise)

          c
          @(fx/on-fx-thread
             (fx/create-component
               {:fx/type fx/ext-watcher
                :ref ref
                :desc {:fx/type (fn update-during-render [{:keys [value]}]
                                  (swap! values conj value)
                                  (when (= 1 value) (reset! ref 2) (reset! ref 0))
                                  (when (and (= 0 value) (> (count @values) 1)) (deliver done true))
                                  {:fx/type :label :text (str value)})}}))]
      (try
        (reset! ref 1)
        (is (= true (deref done 5000 :timeout)))
        (flush-fx!)
        (is (= [0 1 0] @values))
        (finally @(fx/on-fx-thread (fx/delete-component c))))))
  (testing "Queued work and stale callbacks are inert after deletion"
    (let [ref (atom 0)
          renders (atom 0)

          c
          @(fx/on-fx-thread
             (fx/create-component {:fx/type fx/ext-watcher
                                   :ref ref
                                   :desc {:fx/type (fn counted-child [_]
                                                     (swap! renders inc)
                                                     {:fx/type :label})}}))

          old-watch
          (reduce-kv (fn [_ _ callback] callback) nil (.getWatches ref))]
      @(fx/on-fx-thread (reset! ref 1) (fx/delete-component c) (old-watch nil ref 1 2))
      (flush-fx!)
      (is (= 1 @renders))
      (is (= {} (.getWatches ref)))))
  (testing "Manual roots use the current opts handler and recover after a failure"
    (let [ref (atom false)
          errors (atom [])

          first-handler
          (fn first-handler [e]
            (swap! errors conj [:first (ex-message e)]))

          second-handler
          (fn second-handler [e]
            (swap! errors conj [:second (ex-message e)]))

          desc
          {:fx/type fx/ext-watcher
           :ref ref
           :desc {:fx/type (fn failing-manual-child [{:keys [value]}]
                             (when value (throw (ex-info "manual failure" {})))
                             {:fx/type :label :text "recovered"})}}

          current
          (volatile! @(fx/on-fx-thread
                        (fx/create-component desc {:fx.opt/error-handler first-handler})))

          label (fx/instance @current)]
      (try
        (reset! ref true)
        (flush-fx!)
        (is (= [[:first "manual failure"]] @errors))
        (reset! ref false)
        (flush-fx!)
        (is (= "recovered" (.getText ^Label label)))
        (vswap! current
                (fn advance-manual-root [c]
                  @(fx/on-fx-thread
                     (fx/advance-component c desc {:fx.opt/error-handler second-handler}))))
        (reset! ref true)
        (flush-fx!)
        (is (= [[:first "manual failure"] [:second "manual failure"]] @errors))
        (finally @(fx/on-fx-thread (fx/delete-component @current))))))
  (testing "Async failures reach the renderer handler and a later update recovers"
    (doseq [use-opts [false true]]
      (let [ref (atom false)
            errors (atom [])

            renderer
            (if use-opts
              (fx/create-renderer :opts {:fx.opt/error-handler #(swap! errors conj %)})
              (fx/create-renderer :error-handler #(swap! errors conj %)))

            desc
            {:fx/type fx/ext-watcher
             :ref ref
             :desc {:fx/type (fn failing-child [{:keys [value]}]
                               (when value (throw (ex-info "expected render failure" {})))
                               {:fx/type :label :text "recovered"})}}]
        (try
          @(renderer desc)
          (reset! ref true)
          (flush-fx!)
          (is (= ["expected render failure"] (mapv ex-message @errors)))
          (reset! ref false)
          (flush-fx!)
          (is (= 1 (count @errors)))
          (finally @(renderer nil)))))))

;; A background update racing a render causes another pass.
;; Advancement must remain serialized.
(deftest background-invalidation-during-render-test
  (let [ref (atom 0)
        entered (promise)
        release (promise)
        finished (promise)
        values (atom [])

        c
        @(fx/on-fx-thread
           (fx/create-component
             {:fx/type fx/ext-watcher
              :ref ref
              :desc {:fx/type (fn gated-child [{:keys [value]}]
                                (swap! values conj value)
                                (when (= 1 value)
                                  (deliver entered true)
                                  (when-not (= true (deref release 5000 :timeout))
                                    (throw (ex-info "Background update did not arrive" {}))))
                                (when (= 2 value) (deliver finished true))
                                {:fx/type :label :text (str value)})}}))

        worker
        (future
          (when (= true (deref entered 5000 :timeout))
            (reset! ref 2))
          (deliver release true))]
    (try
      (reset! ref 1)
      (is (= true (deref finished 5000 :timeout)))
      (flush-fx!)
      (is (= [0 1 2] @values))
      (is (= "2" (.getText ^Label (fx/instance c))))
      (finally
        (deliver release true)
        @worker
        @(fx/on-fx-thread (fx/delete-component c))))))

;; Middleware deriving output from child instances also reconciles.
(deftest renderer-middleware-reconciliation-test
  (let [ref (atom true)

        renderer
        (fx/create-renderer
          :middleware #(lifecycle/wrap-coerce %
                                              (fn derive-output [instance]
                                                (if instance :present :absent))))

        desc
        {:fx/type fx/ext-watcher
         :ref ref
         :desc {:fx/type (fn conditional-output [{:keys [value]}]
                           (when value {:fx/type :label :text "present"}))}}

        current @(renderer desc)]
    (try
      (is (= :present (component/instance current)))
      (reset! ref false)
      (flush-fx!)
      (is (= :absent (component/instance current)))
      (reset! ref true)
      (flush-fx!)
      (is (= :present (component/instance current)))
      (finally @(renderer nil)))))


;; Deferred factory output can replace its content and releases its watches on removal.
(deftest factory-root-ownership-test
  (testing "Pagination owns a stable page whose watched content can change type"
    (let [ref (atom :label)
          desc {:fx/type :pagination
                :page-count 1
                :page-factory
                (fn describe-page [index]
                  (when (= 0 index)
                    {:fx/type fx/ext-watcher
                     :ref ref
                     :desc {:fx/type (fn page-content [{:keys [value]}]
                                       (case value
                                         :label {:fx/type :label :id "reactive-page"}
                                         :field {:fx/type :text-field}
                                         nil))}}))}
          c @(fx/on-fx-thread (fx/create-component desc))
          ^Pagination pagination (fx/instance c)]
      (try
        (let [^StackPane host
              @(fx/on-fx-thread
                 (Scene. pagination)
                 (.applyCss pagination)
                 (.getParent (.lookup pagination "#reactive-page")))]
          (is (some? (.getParent host)))
          (doseq [[value klass] [[:none nil] [:field TextField] [:label Label]]]
            (reset! ref value)
            (flush-fx!)
            (is (some? (.getParent host)))
            (if klass
              (is (instance? klass (first (.getChildren host))))
              (is (empty? (.getChildren host)))))
          @(fx/on-fx-thread
             (is (nil? (.call (.getPageFactory pagination) 1)))
             ;; The skin can call the factory to see whether a page exists.
             (.call (.getPageFactory pagination) 0))
          (flush-fx!)
          (is (= 1 (count (.getWatches ref))))
          @(fx/on-fx-thread (.remove (.getChildren ^Pane (.getParent host)) host))
          (flush-fx!)
          (is (empty? (.getWatches ref))))
        (finally @(fx/on-fx-thread (fx/delete-component c))))))
  (testing "A deferred cell can replace its graphic and releases it when emptied"
    (let [ref (atom true)
          desc {:fx/type :list-view
                :items [:item]
                :cell-factory (fn describe-cell [_]
                                {:graphic {:fx/type fx/ext-watcher
                                           :ref ref
                                           :desc {:fx/type (fn visible-graphic [{:keys [value]}]
                                                             (when value {:fx/type :label}))}}})}
          c @(fx/on-fx-thread (fx/create-component desc))
          ^ListView list-view (fx/instance c)]
      (try
        (let [^ListCell cell @(fx/on-fx-thread
                                (doto ^ListCell (.call (.getCellFactory list-view) list-view)
                                  (.updateListView list-view)
                                  (.updateIndex 0)))]
          (is (instance? Label (.getGraphic cell)))
          (reset! ref false)
          (flush-fx!)
          (is (nil? (.getGraphic cell)))
          (reset! ref true)
          (flush-fx!)
          (is (instance? Label (.getGraphic cell)))
          @(fx/on-fx-thread (.updateIndex cell -1))
          (is (nil? (.getGraphic cell)))
          (is (empty? (.getWatches ref))))
        (finally @(fx/on-fx-thread (fx/delete-component c)))))))
