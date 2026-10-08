(ns cljfx.watcher-test
  (:require [cljfx.api :as fx]
            [cljfx.fx.label :as fx.label]
            [cljfx.fx.text-field :as fx.text-field]
            [cljfx.lifecycle :as lifecycle]
            [clojure.test :refer [deftest is]])
  (:import [clojure.lang IRef]
           [javafx.scene.control Label TextField]))

(deftest dedupe-on-fx-thread
  (let [state (atom 0)
        refresh-counter (atom 0)
        component (fx/create-component
                    {:fx/type fx/ext-watcher
                     :ref state
                     :desc {:fx/type (fn [{:keys [value key]}]
                                       (swap! refresh-counter inc)
                                       {:fx/type :label
                                        :text (str value key)})
                            :key :test}})
        fx-thread-blocked (promise)
        release-fx-thread (promise)]
    (fx/instance component)
    (fx/run-later
      (deliver fx-thread-blocked true)
      @release-fx-thread)
    @fx-thread-blocked
    (try
      (dotimes [_ 100000]
        (swap! state inc))
      (deliver release-fx-thread true)
      @(fx/run-later)
      (is (= 2 @refresh-counter))
      (finally
        (deliver release-fx-thread true)
        (fx/delete-component component)))))

(deftest key-test
  (let [state (atom "Foo")
        ^Label label (fx/instance
                       (fx/create-component
                         {:fx/type fx/ext-watcher
                          :ref state
                          :key :text
                          :desc {:fx/type :label}}))]
    (is (= "Foo" (.getText label)))
    (reset! state "Bar")
    (is (= "Bar" @(fx/run-later (.getText label))))))

(deftest key-advance-test
  (let [^IRef state (atom "Some text")
        c1 (fx/create-component
             {:fx/type fx/ext-watcher
              :ref state
              :key :text
              :desc {:fx/type :text-field}})
        ^TextField text-field (fx/instance c1)
        _ (is (= "Some text" (.getText text-field)))
        c2 (fx/advance-component
             c1
             {:fx/type fx/ext-watcher
              :ref state
              :key :prompt-text
              :desc {:fx/type :text-field}})
        _ (is (nil? (.getText text-field)))
        _ (is (= "Some text" (.getPromptText text-field)))
        _ (reset! state "Another text")
        _ @(fx/run-later "Waiting...")
        _ @(fx/run-later "Waiting...")
        _ @(fx/run-later "Waiting...")
        _ @(fx/run-later "Waiting...")
        _ (is (= "Another text" (.getPromptText text-field)))
        _ (is (nil? (.getText text-field)))]
    (fx/delete-component c2)
    (is (= {} (.getWatches state)))))

;; Switching between refs and nil must preserve nested local state and native controls.
(deftest ref-advance-preserves-local-state-test
  (let [^IRef a (atom "A")
        ^IRef b (atom "B")
        update-state (atom nil)
        component (atom nil)
        view (fn [{:keys [state swap-state value]}]
               (reset! update-state swap-state)
               {:fx/type fx.text-field/lifecycle
                :text state
                :prompt-text value})
        desc (fn [ref]
               {:fx/type fx/ext-watcher
                :ref ref
                :desc {:fx/type (fn [{:keys [value]}]
                                  {:fx/type fx/ext-state
                                   :initial-state "initial"
                                   :desc {:fx/type view :value value}})}})]
    (try
      (reset! component @(fx/on-fx-thread (fx/create-component (desc nil))))
      (let [^TextField text-field (fx/instance @component)]
        (is (nil? (.getPromptText text-field)))
        (@update-state (constantly "edited"))
        @(fx/run-later)
        (doseq [[ref value] [[nil nil] [a "A"] [b "B"] [nil nil] [b "B"]]]
          (reset! component @(fx/on-fx-thread (fx/advance-component @component (desc ref))))
          (is (identical? text-field (fx/instance @component)))
          (is (= "edited" (.getText text-field)))
          (is (= value (.getPromptText text-field)))
          (is (= (if (identical? ref a) 1 0) (count (.getWatches a))))
          (is (= (if (identical? ref b) 1 0) (count (.getWatches b)))))
        (reset! b "updated")
        @(fx/run-later)
        (is (= "updated" (.getPromptText text-field)))
        (is (= "edited" (.getText text-field))))
      (finally
        (when @component
          @(fx/on-fx-thread (fx/delete-component @component)))))
    (is (= {} (.getWatches a)))
    (is (= {} (.getWatches b)))))

;; Old queued renders and in-flight callbacks must not overwrite the replacement ref's value.
(deftest ref-advance-invalidates-old-subscription-test
  (let [^IRef a (atom "A")
        ^IRef b (atom "B")
        rendered-values (atom [])
        component (atom nil)
        desc (fn [ref]
               {:fx/type fx/ext-watcher
                :ref ref
                :key :text
                :desc {:fx/type (fn [{:keys [text]}]
                                  (swap! rendered-values conj text)
                                  {:fx/type fx.label/lifecycle :text text})}})]
    (try
      (reset! component @(fx/on-fx-thread (fx/create-component (desc a))))
      (let [label (fx/instance @component)
            old-watch (reduce-kv (fn [_ _ callback] callback) nil (.getWatches a))]
        @(fx/on-fx-thread
           (reset! a "queued old value")
           (reset! component (fx/advance-component @component (desc b)))
           (old-watch nil a "A" "late old value"))
        @(fx/run-later)
        (is (identical? label (fx/instance @component)))
        (is (= ["A" "B"] @rendered-values))
        (is (= {} (.getWatches a)))
        (is (= 1 (count (.getWatches b))))
        @(fx/on-fx-thread
           (reset! b "queued before nil")
           (reset! component (fx/advance-component @component (desc nil))))
        @(fx/run-later)
        (is (= ["A" "B" nil] @rendered-values))
        (is (= {} (.getWatches b)))
        (reset! a "unobserved A")
        (reset! b "unobserved B")
        @(fx/run-later)
        (is (= ["A" "B" nil] @rendered-values)))
      (finally
        (when @component
          @(fx/on-fx-thread (fx/delete-component @component)))))))

;; A ref switch may still replace the root when the description changes its lifecycle.
(deftest ref-advance-allows-root-replacement-test
  (let [^IRef a (atom "A")
        ^IRef b (atom "B")
        component (atom nil)]
    (try
      (reset! component
              @(fx/on-fx-thread
                 (fx/create-component {:fx/type fx/ext-watcher
                                       :ref a
                                       :key :text
                                       :desc {:fx/type fx.label/lifecycle}})))
      (let [label (fx/instance @component)]
        (reset! component
                @(fx/on-fx-thread
                   (fx/advance-component @component
                                         {:fx/type fx/ext-watcher
                                          :ref b
                                          :key :text
                                          :desc {:fx/type fx.text-field/lifecycle}})))
        (is (not (identical? label (fx/instance @component))))
        (is (= "B" (.getText ^TextField (fx/instance @component)))))
      (finally
        (when @component
          @(fx/on-fx-thread (fx/delete-component @component)))))
    (is (= {} (.getWatches a)))
    (is (= {} (.getWatches b)))))

;; Explicit recreation on the ref key must still reset the child and its local state.
(deftest ref-key-change-recreates-child-test
  (let [^IRef a (atom "A")
        ^IRef b (atom "B")
        update-state (atom nil)
        component (atom nil)
        desc (fn [ref]
               {:fx/type fx/ext-recreate-on-key-changed
                :key ref
                :desc {:fx/type fx/ext-watcher
                       :ref ref
                       :desc {:fx/type fx/ext-state
                              :initial-state "initial"
                              :desc {:fx/type (fn [{:keys [state swap-state]}]
                                                (reset! update-state swap-state)
                                                {:fx/type fx.text-field/lifecycle
                                                 :text state})}}}})]
    (try
      (reset! component @(fx/on-fx-thread (fx/create-component (desc a))))
      (let [text-field (fx/instance @component)]
        (@update-state (constantly "edited"))
        @(fx/run-later)
        (is (= "edited" (.getText ^TextField text-field)))
        (reset! component @(fx/on-fx-thread (fx/advance-component @component (desc b))))
        (is (not (identical? text-field (fx/instance @component))))
        (is (= "initial" (.getText ^TextField (fx/instance @component)))))
      (finally
        (when @component
          @(fx/on-fx-thread (fx/delete-component @component)))))
    (is (= {} (.getWatches a)))
    (is (= {} (.getWatches b)))))

;; Creation and ref replacement must retain updates made while the child is initializing.
(deftest updates-during-child-initialization-test
  (doseq [phase [:create :advance-from-nil :advance-from-ref]]
    (let [^IRef a (atom "A")
          ^IRef b (atom "B")
          rendered-values (atom [])
          child (reify lifecycle/Lifecycle
                  (create [_ {:keys [value]} _]
                    (swap! rendered-values conj value)
                    (when (= "B" value)
                      (reset! b "updated during creation"))
                    (Object.))
                  (advance [_ component {:keys [value]} _]
                    (swap! rendered-values conj value)
                    (when (= "B" value)
                      (reset! b "updated during advancement"))
                    component)
                  (delete [_ _ _]))
          component (atom nil)
          desc (fn [ref]
                 {:fx/type fx/ext-watcher
                  :ref ref
                  :desc {:fx/type child}})]
      (try
        @(fx/on-fx-thread
           (if (= :create phase)
             (reset! component (fx/create-component (desc b)))
             (let [initial-ref (when (= :advance-from-ref phase) a)]
               (reset! component (fx/create-component (desc initial-ref)))
               (reset! component (fx/advance-component @component (desc b))))))
        @(fx/run-later)
        (is (= (if (= :create phase)
                 ["B" "updated during creation"]
                 [(when (= :advance-from-ref phase) "A") "B" "updated during advancement"])
               @rendered-values))
        (is (= {} (.getWatches a)))
        (is (= 1 (count (.getWatches b))))
        (finally
          (when @component
            @(fx/on-fx-thread (fx/delete-component @component)))))
      (is (= {} (.getWatches b))))))
