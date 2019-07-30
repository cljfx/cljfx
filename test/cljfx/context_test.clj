(ns cljfx.context-test
  (:require [clojure.test :refer :all]
            [testit.core :refer :all]
            [cljfx.context :as context]))

(deftest context-allows-subscriptions-to-call-each-other
  (let [context (context/create {:db 1} identity)
        inc-db #(inc (context/sub % :db))]
    (fact
      (context/sub context inc-db) => 2)))

(deftest subscription-results-are-memoized
  (let [*tracker (atom 0)
        context (context/create {} identity)
        f (fn fib [context n]
            (swap! *tracker inc)
            (if (< n 2)
              1
              (+ (context/sub context fib (- n 2))
                 (context/sub context fib (- n 1)))))]
    (context/sub context f 10)
    (fact
      @*tracker => 11))
  (testing "a missing entry is treated like a nil entry"
    (let [context (context/create {} identity)
          f #(context/sub % :entry)
          _ (fact (context/sub context f) => nil)
          context (context/swap context assoc :entry 0)
          _ (fact (context/sub context f) => 0)
          ])))

(deftest after-changing-context-only-affected-subscriptions-are-recalculated
  (let [*greeting-call-counter (atom 0)
        *username-call-counter (atom 0)
        context-1 (context/create {:db {:user {:name "vlaaad"}}} identity)
        username (fn [context]
                   (swap! *username-call-counter inc)
                   (-> (context/sub context :db) :user :name))
        greeting (fn [context]
                   (swap! *greeting-call-counter inc)
                   (str "Hello, " (context/sub context username) "!"))]
    (facts
      (context/sub context-1 greeting) => "Hello, vlaaad!"
      @*username-call-counter => 1
      @*greeting-call-counter => 1)
    (let [context-2 (context/swap context-1 assoc-in [:db :user :age] 28)]
      (facts
        "Greeting is unchanged"
        (context/sub context-2 greeting) => "Hello, vlaaad!"
        (context/sub context-2 username) => "vlaaad")
      (fact
        "Since [:db] changed, [:user-name] is recalculated"
        @*username-call-counter => 2)
      (facts
        "Since after recalculating [:user-name] it stays the same, greeting is not recalculated"
        @*greeting-call-counter => 1))))

(deftest subscription-deps-are-updated-on-recalculation
  (let [*template-call-counter (atom 0)
        context-1 (context/create {:db {:lang :en
                                        :templates {:en "Hello, %s!"
                                                    :ru "Привет, %s!"}}}
                                  identity)
        lang #(:lang (context/sub % :db))
        en-template #(-> % (context/sub :db) :templates :en)
        ru-template #(-> % (context/sub :db) :templates :ru)
        template (fn [context]
                   (swap! *template-call-counter inc)
                   (case (context/sub context lang)
                     :en (context/sub context en-template)
                     :ru (context/sub context ru-template)))]
    (facts
      "Template depends on [:lang] and [:en-template] subscriptions"
      (context/sub context-1 template) => "Hello, %s!"
      @*template-call-counter => 1)
    (let [context-2 (context/swap context-1 assoc-in [:db :lang] :ru)]
      (facts
        "After changing [:lang] [:template] depends on [:lang] and [:ru-template] subscriptions"
        (context/sub context-2 template) => "Привет, %s!"
        @*template-call-counter => 2)
      (let [context-3 (context/swap context-2 assoc-in [:db :templates :en] "Hi, %s!")]
        (facts
          "Since [:template] no longer depends on [:en-template], it's not recalculated"
          (context/sub context-3 template) => "Привет, %s!"
          @*template-call-counter => 2)))))

(deftest creating-derived-contexts-inside-subs-add-dependency-on-context-itself
  (let [*sub-context-call-counter (atom 0)
        context (context/create {:db 1} identity)
        sub-context (fn [context]
                      (swap! *sub-context-call-counter inc)
                      (let [str-db (str (context/sub context :db))]
                        (context/swap context assoc :str-db str-db)))]
    (context/sub context sub-context)
    (let [context-2 (context/swap context assoc :other-db 2)]
      (context/sub context-2 sub-context))
    (fact
      "If update-subs is used inside sub fn, this sub starts to depend on whole context"
      @*sub-context-call-counter => 2)))

(deftest double-invalidations-don't-trigger-recalculations
  (let [*str-db-call-counter (atom 0)
        context-1 (context/create {:db 1} identity)
        str-db (fn [context]
                 (swap! *str-db-call-counter inc)
                 (str (context/sub context :db)))
        res-1 (context/sub context-1 str-db)
        _ (context/swap context-1 assoc :db 2)
        context-3 (context/swap context-1 assoc :db 3)
        res-3 (context/sub context-3 str-db)]
    (facts
      "Values between multiple invalidations are recalculated"
      res-1 => "1"
      res-3 => "3")
    (fact
      "Recalculations happen on demand and not on invalidation"
      @*str-db-call-counter => 2)))

(deftest cross-context-dependencies-are-not-tracked
  (let [*sub-call-counter (atom 0)
        input-context (context/create {:db 1} identity)
        context-a-1 (context/create {:db :a} identity)
        sub (fn [_ other-context]
              (swap! *sub-call-counter inc)
              (context/sub other-context :db))
        _ (context/sub context-a-1 sub input-context)
        context-a-2 (context/swap context-a-1 assoc :db :b)
        _ (context/sub context-a-2 sub input-context)]
    (fact
      "When calling sub to other context inside subscription function, it does not begin to depend on it"
      @*sub-call-counter => 1)))

(deftest contexts-are-repl-friendly
  (let [*leaked-context (atom nil)
        *leaking-sub-call-counter (atom 0)
        context-1 (context/create {:db 1 :other-db :a} identity)
        leaking-sub (fn [context]
                      (swap! *leaking-sub-call-counter inc)
                      (reset! *leaked-context (context/unbind context))
                      (inc (context/sub context :db)))]
    (fact
      (context/sub context-1 leaking-sub) => 2)
    (fact
      (context/sub @*leaked-context :other-db) => :a)
    (let [context-2 (context/swap context-1 assoc :other-db :b)]
      (fact
        (context/sub context-2 leaking-sub) => 2)
      (fact
        "Even though we leaked sub's context and subscribed to it later, it does not add dependency on leaked subscription"
        @*leaking-sub-call-counter => 1))))

(deftest you-can-subscribe-to-functions-directly
  (let [*inc-db-call-counter (atom 0)
        context (context/create {:db 1} identity)
        inc-db (fn [context]
                 (swap! *inc-db-call-counter inc)
                 (inc (context/sub context :db)))
        _ (facts
            "Subscribing to function directly calculates result"
            (context/sub context inc-db) => 2
            @*inc-db-call-counter => 1)
        context-2 (context/swap context update :db inc)
        _ (facts
            "Updating context with dependent value triggers recalculation"
            (context/sub context-2 inc-db) => 3
            @*inc-db-call-counter => 2)
        context-3 (context/swap context-2 assoc :other-db (constantly :a))
        _ (facts
            "Updating context with independent change does not trigger recalculation"
            (context/sub context-3 inc-db) => 3
            @*inc-db-call-counter => 2)]))
