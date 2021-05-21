(ns cljfx.lifecycle-test
  (:require [clojure.test :refer :all]
            [testit.core :refer :all]
            [cljfx.test-helpers :refer :all]
            [cljfx.context :as context]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.component :as component]
            [cljfx.api :as fx])
  (:import [javafx.scene.control Label]))

(deftest env-test
  (let [label (fn [{:keys [a b]}]
                {:fx/type :label
                 :text (str :a " " a ", " :b " " b)})
        c-1 (fx/create-component {:fx/type fx/ext-set-env
                                  :env {:a 1 :b 2}
                                  :desc {:fx/type fx/ext-get-env
                                         :env [:a :b]
                                         :desc {:fx/type label}}})
        ^Label i-1 (fx/instance c-1)
        _ (fact (.getText i-1) => ":a 1, :b 2")
        c-2 (fx/advance-component c-1 {:fx/type fx/ext-set-env
                                       :env {:a 2 :b 2}
                                       :desc {:fx/type fx/ext-get-env
                                              :env [:a :b]
                                              :desc {:fx/type label}}})
        ^Label i-2 (fx/instance c-2)
        _ (fact (.getText i-2) => ":a 2, :b 2")
        c-3 (fx/advance-component c-2 {:fx/type fx/ext-set-env
                                       :env {:x 1 :y 2}
                                       :desc {:fx/type fx/ext-get-env
                                              :env {:x :a
                                                    :y :b}
                                              :desc {:fx/type label}}})
        ^Label i-3 (fx/instance c-3)
        _ (fact (.getText i-3) => ":a 1, :b 2")
        _ (fact (= i-1 i-2 i-3) => true)]))

(deftest wrap-extra-props-test
  (let [extra-props #{:a :b :c}
        existing-props #{:d :e}
        state-m (mk-state {:props {}})
        {:keys [state grab-history props-config]} (mk-props (concat extra-props existing-props)
                                                            state-m)
        {:keys [logging-lifecycle]} (mk-logging-lifecycle state-m)
        lifecycle (-> logging-lifecycle
                      (lifecycle/wrap-extra-props
                        (select-keys props-config extra-props)))

        ;; assign :a/:b:c, forward :d/:e to create
        component (lifecycle/create
                    lifecycle
                    {:a 1
                     :b 2
                     :c 3
                     :d 4
                     :e 5}
                    {::foo 1})
        _ (fact (sort-by-from :prop 1 (grab-history))
                => [{:op :create, :desc {:d 4, :e 5}, :opts #:cljfx.lifecycle-test{:foo 1}}
                    {:op :assign!, :prop :a, :instance :create, :coerced-value "1", :value 1}
                    {:op :assign!, :prop :b, :instance :create, :coerced-value "2", :value 2}
                    {:op :assign!, :prop :c, :instance :create, :coerced-value "3", :value 3}])
        _ (fact (component/instance component)
                => :create)

        ;; replace :b, update instance
        component (lifecycle/advance
                    lifecycle
                    component
                    {:a 1
                     :b 3
                     :c 3
                     :d 4
                     :e 5}
                    {::foo 2})
        _ (fact (sort-by-from :prop 1 (grab-history))
                => [{:op :advance, :component :create, :desc {:d 4, :e 5}, :opts {::foo 2}}
                    ;; FIXME retract props on old instance
                    ;{:op :retract!, :prop :a, :instance :create, :coerced-value "1", :value 1}
                    ;{:op :retract!, :prop :b, :instance :create, :coerced-value "2", :value 2}
                    ;{:op :retract!, :prop :c, :instance :create, :coerced-value "3", :value 3}
                    {:op :assign!, :prop :a, :instance :advance, :coerced-value "1", :value 1}
                    {:op :assign!, :prop :b, :instance :advance, :coerced-value "3", :value 3}
                    {:op :assign!, :prop :c, :instance :advance, :coerced-value "3", :value 3}
                    ])
        _ (fact (component/instance component)
                => :advance)

        ;; replace forwarded :d
        component (lifecycle/advance
                    lifecycle
                    component
                    {:a 1
                     :b 3
                     :c 3
                     :d 2
                     :e 5}
                    {::foo 3})
        _ (fact (sort-by-from :prop 1 (grab-history))
                => [{:op :advance, :component :advance, :desc {:d 2, :e 5}, :opts {::foo 3}}
                    {:op :replace!, :prop :a, :instance :advance, :coerced-new-value "1", :old-value 1, :new-value 1}
                    {:op :replace!, :prop :b, :instance :advance, :coerced-new-value "3", :old-value 3, :new-value 3}
                    {:op :replace!, :prop :c, :instance :advance, :coerced-new-value "3", :old-value 3, :new-value 3}])
        _ (fact (component/instance component)
                => :advance)

        ;; reset all props on instance change
        _ (swap! state assoc :next-advance-instance :advance1)
        _ (fact (component/instance component)
                => :advance)
        component (lifecycle/advance
                    lifecycle
                    component
                    {:a 1
                     :b 3
                     :c 3
                     :d 2
                     :e 5}
                    {::foo 4})
        _ (fact (sort-by-from :prop 1 (grab-history))
                => [{:op :advance, :component :advance, :desc {:d 2, :e 5}, :opts {::foo 4}}
                    ;; FIXME retract props on old instance
                    ;{:op :retract!, :prop :a, :instance :advance, :coerced-value "1", :value 1}
                    ;{:op :retract!, :prop :b, :instance :advance, :coerced-value "3", :value 3}
                    ;{:op :retract!, :prop :c, :instance :advance, :coerced-value "3", :value 3}
                    {:op :assign!, :prop :a, :instance :advance1, :coerced-value "1", :value 1}
                    {:op :assign!, :prop :b, :instance :advance1, :coerced-value "3", :value 3}
                    {:op :assign!, :prop :c, :instance :advance1, :coerced-value "3", :value 3}])
        _ (fact (component/instance component)
                => :advance1)

        ;; delete
        component (lifecycle/delete
                    lifecycle
                    component
                    {::foo 5})
        _ (fact (grab-history)
                => [;; FIXME retract extra props
                    ;{:op :retract!, :prop :a, :instance :advance1, :coerced-value "1", :value 1}
                    ;{:op :retract!, :prop :b, :instance :advance1, :coerced-value "3", :value 3}
                    ;{:op :retract!, :prop :c, :instance :advance1, :coerced-value "3", :value 3}
                    {:op :delete, :component :advance1, :opts {::foo 5}}
                    ])
        _ (fact (component/instance component)
                => :delete)
        ]))

(deftest make-ext-with-props-test
  (let [{:keys [state] :as state-m} (mk-state {:props {}})
        {:keys [grab-history props-config]} (mk-props [:a :b :c] state-m)
        {:keys [logging-lifecycle]} (mk-logging-lifecycle state-m)
        lifecycle (lifecycle/make-ext-with-props
                    logging-lifecycle
                    props-config)

        ;; create component and assign extra props :a/:b/:c
        component (lifecycle/create
                    lifecycle
                    {:fx/type lifecycle
                     :desc 1
                     :props {:a 1
                             :b 2
                             :c 3}}
                    {::foo 1})
        _ (fact (sort-by-from :prop 1 (grab-history))
                => [{:op :create, :desc 1, :opts {::foo 1}}
                    {:op :assign!, :prop :a, :instance :create, :coerced-value "1", :value 1}
                    {:op :assign!, :prop :b, :instance :create, :coerced-value "2", :value 2}
                    {:op :assign!, :prop :c, :instance :create, :coerced-value "3", :value 3}])
        _ (fact (component/instance component)
                => :create)

        ;; advance :a, retract :b/:c, while changing instance
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :desc 1
                     :props {:a 2}}
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :advance, :component :create, :desc 1, :opts {::foo 2}}
                    ;; FIXME retract props on old instance
                    ;{:op :retract!, :prop :a, :instance :create, :coerced-value "1", :value 1}
                    ;{:op :retract!, :prop :b, :instance :create, :coerced-value "2", :value 2}
                    ;{:op :retract!, :prop :c, :instance :create, :coerced-value "3", :value 3}
                    {:op :assign!, :prop :a, :instance :advance, :coerced-value "2", :value 2}
                    ])
        _ (fact (component/instance component)
                => :advance)

        ;; replace :a
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :desc 1
                     :props {:a 2}}
                    {::foo 3})
        _ (fact (grab-history)
                => [{:op :advance, :component :advance, :desc 1, :opts {::foo 3}}
                    {:op :replace!, :prop :a, :instance :advance, :coerced-new-value "2", :old-value 2, :new-value 2}])
        _ (fact (component/instance component)
                => :advance)

        ;; change :desc and update instance
        _ (swap! state assoc :next-advance-instance :advance1)
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :desc 2
                     :props {:a 2}}
                    {::foo 4})
        _ (fact (grab-history)
                => [{:op :advance, :component :advance, :desc 2, :opts {::foo 4}}
                    ;; FIXME retract props on old instance
                    ;{:op :retract!, :prop :a, :instance :advance, :coerced-value "2", :value 2}
                    {:op :assign!, :prop :a, :instance :advance1, :coerced-value "2", :value 2}
                    ])
        _ (fact (component/instance component)
                => :advance1)

        ;; delete
        component (lifecycle/delete
                    lifecycle
                    component
                    {::foo 4})
        _ (fact (grab-history)
                => [;; FIXME retract props on old instance
                    ;{:op :retract!, :prop :a, :instance :advance1, :coerced-value "2", :value 2}
                    {:op :delete, :component :advance1, :opts {::foo 4}}])
        _ (fact (component/instance component)
                => :delete)
        ]))


(deftest wrap-context-desc-test
  (let [{:keys [grab-history logging-lifecycle]} (mk-logging-lifecycle (mk-state {}))
        lifecycle (lifecycle/wrap-context-desc logging-lifecycle)

        ;; create
        context (context/create {:a 1 :b 2} identity)
        component (lifecycle/create
                    lifecycle
                    context
                    {::foo 1})
        _ (fact (grab-history)
                => [{:op :create, :desc context, :opts {::foo 1, :fx/context context}}])
        _ (fact (component/instance component)
                => :create)

        ;; updated context
        context (context/swap context update :a inc)
        component (lifecycle/advance
                    lifecycle
                    component
                    context
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :advance, :component :create, :desc context, :opts {::foo 2, :fx/context context}}])
        _ (fact (component/instance component)
                => :advance)

        ;; delete
        component (lifecycle/delete
                    lifecycle
                    component
                    {::foo 3})
        _ (fact (grab-history)
                => [{:op :delete, :component :advance, :opts {::foo 3}}])
        _ (fact (component/instance component)
                => :delete)
        ]))

(deftest context-fn->dynamic-test
  (let [{:keys [state grab-history logging-lifecycle]} (mk-logging-lifecycle (mk-state {}))
        lifecycle lifecycle/context-fn->dynamic
        type->lifecycle (constantly nil)
        record-sub-context-fn #(swap! state update :history conj
                                      {:op :sub-context-fn
                                       :desc %})

        ;; create
        context0 (context/create {:a 1 :b 2} identity)
        component (lifecycle/create
                    lifecycle
                    {:fx/type (fn [desc]
                                (record-sub-context-fn desc)
                                {:fx/type logging-lifecycle
                                 :d 4})
                     :c 3}
                    {:fx/context context0
                     :fx.opt/type->lifecycle type->lifecycle})
        _ (let [h (grab-history)
                context1 (get-in h [0 :desc :fx/context])]
            (assert context1)
            (fact h
                  => [{:op :sub-context-fn, :desc {:c 3, :fx/context context1}}
                      {:op :create
                       :desc {:fx/type logging-lifecycle
                              :d 4}
                       :opts {:fx/context context0
                              :fx.opt/type->lifecycle type->lifecycle}}]))

        ;; same desc
        context2 (context/create {:a 1 :b 2} identity)
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type (fn [desc]
                                (record-sub-context-fn desc)
                                {:fx/type logging-lifecycle
                                 :d 4})
                     :c 3}
                    {:fx/context context2
                     :fx.opt/type->lifecycle type->lifecycle})
        _ (let [h (grab-history)
                context3 (get-in h [0 :desc :fx/context])]
            (assert context3)
            (fact h
                  => [{:op :sub-context-fn, :desc {:c 3, :fx/context context3}}
                      {:op :advance
                       :component :create
                       :desc {:fx/type logging-lifecycle
                              :d 4}
                       :opts {:fx/context context2
                              :fx.opt/type->lifecycle type->lifecycle}}]))

        ;; delete
        context4 (context/create {:a 1 :b 2} identity)
        component (lifecycle/delete
                    lifecycle
                    component
                    {:fx/context context4
                     :fx.opt/type->lifecycle type->lifecycle})
        _ (fact (grab-history)
                => [{:op :delete
                     :component :advance
                     :opts {:fx/context context4
                            :fx.opt/type->lifecycle type->lifecycle}}])
        ]))

(deftest wrap-on-instance-lifecycle-test
  (let [{:keys [state grab-history logging-lifecycle]} (mk-logging-lifecycle (mk-state {}))
        lifecycle (lifecycle/wrap-on-instance-lifecycle logging-lifecycle)

        on-created (fn [instance]
                     (swap! state update :history conj
                            {:op :on-created
                             :instance instance}))
        on-advanced (fn [old-instance new-instance]
                      (swap! state update :history conj
                             {:op :on-advanced
                              :old-instance old-instance
                              :new-instance new-instance}))
        on-deleted (fn [instance]
                     (swap! state update :history conj
                            {:op :on-deleted
                             :instance instance}))

        ;; create
        component (lifecycle/create
                    lifecycle
                    {:on-created on-created
                     :desc {:a 1}}
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :create, :desc {:a 1}, :opts {::foo 2}}
                    {:op :on-created, :instance :create}])

        ;; advance :a
        component (lifecycle/advance
                    lifecycle
                    component
                    {:on-advanced on-advanced
                     :desc {:a 2}}
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :advance, :component :create, :desc {:a 2}, :opts {::foo 2}}
                    {:op :on-advanced, :old-instance :create, :new-instance :advance}])

        ;; same instance, no on-advanced call
        component (lifecycle/advance
                    lifecycle
                    component
                    {:on-advanced on-advanced
                     :desc {:a 2}}
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :advance, :component :advance, :desc {:a 2}, :opts {::foo 2}}])

        ;; different instance, trigger on-advanced
        _ (swap! state assoc :next-advance-instance :advance1)
        component (lifecycle/advance
                    lifecycle
                    component
                    {:on-advanced on-advanced
                     :desc {:a 2}}
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :advance, :component :advance, :desc {:a 2}, :opts {::foo 2}}
                    {:op :on-advanced, :old-instance :advance, :new-instance :advance1}])

        ;; delete w/o :on-deleted
        component (lifecycle/delete
                    lifecycle
                    component
                    {::foo 2})
        _ (fact (grab-history)
                => [{:op :delete, :component :advance1, :opts {::foo 2}}])

        ;; delete with :on-deleted from create
        component (as-> (lifecycle/create
                          lifecycle
                          {:on-deleted on-deleted}
                          {::foo 2})
                    component
                    (lifecycle/delete
                      lifecycle
                      component
                      {::foo 2}))
        _ (fact (grab-history)
                => [{:op :create, :desc nil, :opts {::foo 2}}
                    {:op :delete, :component :create, :opts {::foo 2}}
                    {:op :on-deleted, :instance :create}])

        ;; delete with :on-deleted from advance
        component (as-> (lifecycle/create
                          lifecycle
                          {}
                          {::foo 2})
                    component
                    (lifecycle/advance
                      lifecycle
                      component
                      {:on-deleted on-deleted}
                      {::foo 2})
                    (lifecycle/delete
                      lifecycle
                      component
                      {::foo 2}))
        _ (fact (grab-history)
                => [{:op :create, :desc nil, :opts {::foo 2}}
                    {:op :advance, :component :create, :desc nil, :opts {::foo 2}}
                    {:op :delete, :component :advance, :opts {::foo 2}}
                    {:op :on-deleted, :instance :advance}])
        ]))

(deftest instance-factory-test
  (let [{:keys [state grab-history]} (mk-state {})
        lifecycle lifecycle/instance-factory
        mk-create (fn [result]
                    (fn []
                      (swap! state update :history conj
                             {:op :create
                              :result result})
                      result))

        create0 (mk-create :create0)
        component (lifecycle/create
                    lifecycle
                    {:create create0}
                    nil)
        _ (fact (grab-history)
                => [{:op :create, :result :create0}])
        _ (fact (component/instance component)
                => :create0)

        ;; same create, no call
        component (lifecycle/advance
                    lifecycle
                    component
                    {:create create0, :result :create0}
                    nil)
        _ (fact (grab-history)
                => [])
        _ (fact (component/instance component)
                => :create0)

        ;; new create, no call
        create1 (mk-create :create1)
        component (lifecycle/advance
                    lifecycle
                    component
                    {:create create1, :result :create1}
                    nil)
        _ (fact (grab-history)
                => [{:op :create, :result :create1}])
        _ (fact (component/instance component)
                => :create1)

        ;; delete
        component (lifecycle/delete
                    lifecycle
                    component
                    nil)
        _ (fact (grab-history)
                => [])
        _ (fact (component/instance component)
                => nil)
        ]))
