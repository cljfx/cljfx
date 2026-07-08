(ns cljfx.lifecycle-test
  (:require [cljfx.api :as fx]
            [cljfx.component :as component]
            [cljfx.context :as context]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]
            [cljfx.test-helpers :refer :all]
            [clojure.test :refer :all]
            [testit.core :refer :all])
  (:import [javafx.beans.value ChangeListener]
           [javafx.scene.control Label TextField]))

(deftest advance-prop-map-preserves-nil-valued-props-test
  (let [events (atom [])
        prop-lifecycle (reify lifecycle/Lifecycle
                         (create [_ desc _]
                           (swap! events conj [:create desc])
                           desc)
                         (advance [_ component desc _]
                           (swap! events conj [:advance component desc])
                           desc)
                         (delete [_ component _]
                           (swap! events conj [:delete component])))
        prop-mutator (reify mutator/Mutator
                       (assign! [_ _ _ value]
                         (swap! events conj [:assign value]))
                       (replace! [_ _ _ old-value new-value]
                         (swap! events conj [:replace old-value new-value]))
                       (retract! [_ _ _ value]
                         (swap! events conj [:retract value])))
        props-config {:x (prop/make prop-mutator prop-lifecycle)}]
    (fact (lifecycle/advance-prop-map {:x nil} {:x nil} props-config (Object.) nil)
          => {:x nil})
    (fact @events
          => [[:advance nil nil]
              [:replace nil nil]])))

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

(deftest root-binds-in-progress-while-delegating-test
  (let [events (atom [])
        child-lifecycle (reify lifecycle/Lifecycle
                          (create [_ desc opts]
                            (swap! events conj [:create lifecycle/*in-progress?* desc opts])
                            (with-meta {:instance (:value desc)}
                                       {`component/instance :instance}))
                          (advance [_ component desc opts]
                            (swap! events conj [:advance lifecycle/*in-progress?* component desc opts])
                            (with-meta {:instance (:value desc)}
                                       {`component/instance :instance}))
                          (delete [_ component opts]
                            (swap! events conj [:delete lifecycle/*in-progress?* component opts])))
        opts {:fx.opt/type->lifecycle identity}
        desc-1 {:fx/type child-lifecycle :value 1}
        desc-2 {:fx/type child-lifecycle :value 2}
        component-1 (lifecycle/create lifecycle/root desc-1 opts)
        component-2 (lifecycle/advance lifecycle/root component-1 desc-2 opts)
        _ (lifecycle/delete lifecycle/root component-2 opts)]
    (fact (map first @events)
          => [:create :advance :delete])
    (fact (map second @events)
          => [true true true])
    (fact (component/instance component-1)
          => 1)
    (fact (component/instance component-2)
          => 2)))

(deftest dynamic-advances-same-lifecycle-and-recreates-changed-lifecycle-test
  (let [events (atom [])
        lifecycle-1 (reify lifecycle/Lifecycle
                      (create [_ desc opts]
                        (swap! events conj [:create-1 desc opts])
                        (with-meta {:desc desc :instance [:lifecycle-1 (:value desc)]}
                                   {`component/instance :instance}))
                      (advance [_ component desc opts]
                        (swap! events conj [:advance-1 (:desc component) desc opts])
                        (with-meta {:desc desc :instance [:lifecycle-1 (:value desc)]}
                                   {`component/instance :instance}))
                      (delete [_ component opts]
                        (swap! events conj [:delete-1 (:desc component) opts])))
        lifecycle-2 (reify lifecycle/Lifecycle
                      (create [_ desc opts]
                        (swap! events conj [:create-2 desc opts])
                        (with-meta {:desc desc :instance [:lifecycle-2 (:value desc)]}
                                   {`component/instance :instance}))
                      (advance [_ component desc opts]
                        (swap! events conj [:advance-2 (:desc component) desc opts])
                        (with-meta {:desc desc :instance [:lifecycle-2 (:value desc)]}
                                   {`component/instance :instance}))
                      (delete [_ component opts]
                        (swap! events conj [:delete-2 (:desc component) opts])))
        opts {:fx.opt/type->lifecycle identity}
        desc-1 {:fx/type lifecycle-1 :value 1}
        desc-2 {:fx/type lifecycle-1 :value 2}
        desc-3 {:fx/type lifecycle-2 :value 3}
        component-1 (lifecycle/create lifecycle/dynamic desc-1 opts)
        component-2 (lifecycle/advance lifecycle/dynamic component-1 desc-2 opts)
        component-3 (lifecycle/advance lifecycle/dynamic component-2 desc-3 opts)
        _ (lifecycle/delete lifecycle/dynamic component-3 opts)]
    (fact (component/instance component-1)
          => [:lifecycle-1 1])
    (fact (component/instance component-2)
          => [:lifecycle-1 2])
    (fact (component/instance component-3)
          => [:lifecycle-2 3])
    (fact @events
          => [[:create-1 desc-1 opts]
              [:advance-1 desc-1 desc-2 opts]
              [:delete-1 desc-2 opts]
              [:create-2 desc-3 opts]
              [:delete-2 desc-3 opts]])))

(deftest wrap-coerce-recoerces-only-when-child-instance-changes-test
  (let [coerced (atom [])
        events (atom [])
        child-lifecycle (reify lifecycle/Lifecycle
                          (create [_ desc opts]
                            (swap! events conj [:create desc opts])
                            (with-meta {:desc desc
                                        :instance (:instance desc)}
                                       {`component/instance :instance}))
                          (advance [_ component desc opts]
                            (swap! events conj [:advance (:desc component) desc opts])
                            (with-meta {:desc desc
                                        :instance (:instance desc)}
                                       {`component/instance :instance}))
                          (delete [_ component opts]
                            (swap! events conj [:delete (:desc component) opts])))
        lifecycle (lifecycle/wrap-coerce child-lifecycle
                                         (fn [x]
                                           (swap! coerced conj x)
                                           [:coerced x]))
        component-1 (lifecycle/create lifecycle {:instance 1 :other :a} {::foo 1})
        component-2 (lifecycle/advance lifecycle component-1 {:instance 1 :other :b} {::foo 2})
        component-3 (lifecycle/advance lifecycle component-2 {:instance 2 :other :c} {::foo 3})
        _ (lifecycle/delete lifecycle component-3 {::foo 4})]
    (fact @coerced
          => [1 2])
    (fact (component/instance component-1)
          => [:coerced 1])
    (fact (component/instance component-2)
          => [:coerced 1])
    (fact (component/instance component-3)
          => [:coerced 2])
    (fact @events
          => [[:create {:instance 1 :other :a} {::foo 1}]
              [:advance {:instance 1 :other :a} {:instance 1 :other :b} {::foo 2}]
              [:advance {:instance 1 :other :b} {:instance 2 :other :c} {::foo 3}]
              [:delete {:instance 2 :other :c} {::foo 4}]])))

(deftest event-handler-preserves-same-kind-components-test
  (binding [lifecycle/*in-progress?* false]
    (let [events (atom [])
          handler-1 #(swap! events conj [:handler-1 %])
          handler-2 #(swap! events conj [:handler-2 %])
          opts-1 {:fx.opt/map-event-handler #(swap! events conj [:map-1 %])}
          opts-2 {:fx.opt/map-event-handler #(swap! events conj [:map-2 %])}
          map-component-1 (lifecycle/create lifecycle/event-handler {:a 1} opts-1)
          map-component-2 (lifecycle/advance lifecycle/event-handler map-component-1 {:a 2} opts-1)
          map-component-3 (lifecycle/advance lifecycle/event-handler map-component-2 {:a 3} opts-2)
          fn-component-1 (lifecycle/create lifecycle/event-handler handler-1 nil)
          fn-component-2 (lifecycle/advance lifecycle/event-handler fn-component-1 handler-2 nil)
          else-component-1 (lifecycle/create lifecycle/event-handler :handler nil)
          else-component-2 (lifecycle/advance lifecycle/event-handler else-component-1 :handler nil)
          else-component-3 (lifecycle/advance lifecycle/event-handler else-component-2 :other nil)]
      ((component/instance map-component-1) :event-1)
      ((component/instance map-component-2) :event-2)
      ((component/instance map-component-3) :event-3)
      ((component/instance fn-component-1) :event-4)
      ((component/instance fn-component-2) :event-5)
      (fact (identical? map-component-1 map-component-2)
            => true)
      (fact (identical? map-component-2 map-component-3)
            => false)
      (fact (identical? fn-component-1 fn-component-2)
            => true)
      (fact (identical? else-component-1 else-component-2)
            => true)
      (fact (identical? else-component-2 else-component-3)
            => false)
      (fact @events
            => [[:map-1 {:a 2 :fx/event :event-1}]
                [:map-1 {:a 2 :fx/event :event-2}]
                [:map-2 {:a 3 :fx/event :event-3}]
                [:handler-2 :event-4]
                [:handler-2 :event-5]]))))

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
                => [{:op :create, :desc {:d 4, :e 5}, :opts {::foo 1}}
                    {:op :assign!, :prop :a, :instance :create, :coerced-value "1", :value 1}
                    {:op :assign!, :prop :b, :instance :create, :coerced-value "2", :value 2}
                    {:op :assign!, :prop :c, :instance :create, :coerced-value "3", :value 3}])
        _ (fact (component/instance component)
                => :create)

        ;; TODO test create => advance without instance change
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
                    {:op :assign!, :prop :a, :instance :advance, :coerced-value "1", :value 1}
                    {:op :assign!, :prop :b, :instance :advance, :coerced-value "3", :value 3}
                    {:op :assign!, :prop :c, :instance :advance, :coerced-value "3", :value 3}])
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
                => [{:op :delete, :component :advance1, :opts {::foo 5}}])
        _ (fact (component/instance component)
                => :delete)]))

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

        ;; TODO test create => advance without instance change
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
                    {:op :assign!, :prop :a, :instance :advance, :coerced-value "2", :value 2}])
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
                    {:op :assign!, :prop :a, :instance :advance1, :coerced-value "2", :value 2}])
        _ (fact (component/instance component)
                => :advance1)

        ;; delete
        component (lifecycle/delete
                    lifecycle
                    component
                    {::foo 4})
        _ (fact (grab-history)
                => [{:op :delete, :component :advance1, :opts {::foo 4}}])
        _ (fact (component/instance component)
                => :delete)]))


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

        ;; TODO test create => advance without instance change
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
                => :delete)]))

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
                            :fx.opt/type->lifecycle type->lifecycle}}])]))

;; TODO test component/instance throughout
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

        ;; TODO test create => advance without instance change
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
                    {:op :on-deleted, :instance :advance}])]))

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
                => nil)]))

(defn- tracking-lifecycle [events]
  (let [next-id (atom 0)]
    (reify lifecycle/Lifecycle
      (create [_ desc opts]
        (let [id (swap! next-id inc)]
          (swap! events conj [:create id desc opts])
          (with-meta
            {:id id
             :desc desc}
            {`component/instance :id})))
      (advance [_ component desc opts]
        (swap! events conj [:advance (:id component) (:desc component) desc opts])
        (assoc component :desc desc))
      (delete [_ component opts]
        (swap! events conj [:delete (:id component) opts])))))

(deftest wrap-many-preserves-key-occurrences-test
  (let [events (atom [])
        lifecycle (lifecycle/wrap-many (tracking-lifecycle events))
        component (lifecycle/create
                    lifecycle
                    [{:fx/key :a :v 1}
                     {:fx/key :a :v 2}
                     {:v 3}
                     {:v 4}]
                    {::foo 1})
        _ (fact (component/instance component)
                => [1 2 3 4])
        _ (fact @events
                => [[:create 1 {:v 1} {::foo 1}]
                    [:create 2 {:v 2} {::foo 1}]
                    [:create 3 {:v 3} {::foo 1}]
                    [:create 4 {:v 4} {::foo 1}]])
        _ (reset! events [])
        component (lifecycle/advance
                    lifecycle
                    component
                    [{:v 30}
                     {:fx/key :a :v 10}
                     {:fx/key :a :v 20}
                     {:v 40}]
                    {::foo 2})
        _ (fact (component/instance component)
                => [3 1 2 4])
        _ (fact (set @events)
                => #{[:advance 1 {:v 1} {:v 10} {::foo 2}]
                     [:advance 2 {:v 2} {:v 20} {::foo 2}]
                     [:advance 3 {:v 3} {:v 30} {::foo 2}]
                     [:advance 4 {:v 4} {:v 40} {::foo 2}]})
        _ (reset! events [])
        component (lifecycle/advance
                    lifecycle
                    component
                    [{:fx/key :a :v 11}
                     {:v 31}]
                    {::foo 3})
        _ (fact (component/instance component)
                => [1 3])
        _ (fact (set @events)
                => #{[:advance 1 {:v 10} {:v 11} {::foo 3}]
                     [:advance 3 {:v 30} {:v 31} {::foo 3}]
                     [:delete 2 {::foo 3}]
                     [:delete 4 {::foo 3}]})
        _ (reset! events [])
        _ (lifecycle/delete lifecycle component {::foo 4})]
    (fact (set @events)
          => #{[:delete 1 {::foo 4}]
               [:delete 3 {::foo 4}]})))

(deftest wrap-many-uses-custom-key-and-child-desc-test
  (let [events (atom [])
        lifecycle (lifecycle/wrap-many
                    (tracking-lifecycle events)
                    :id
                    #(select-keys % [:payload]))
        component (lifecycle/create
                    lifecycle
                    [{:id 1 :payload "a" :ignored true}]
                    nil)
        _ (fact (component/instance component)
                => [1])
        _ (fact @events
                => [[:create 1 {:payload "a"} nil]])
        _ (reset! events [])
        component (lifecycle/advance
                    lifecycle
                    component
                    [{:id 1 :payload "b" :ignored false}]
                    nil)]
    (fact (component/instance component)
          => [1])
    (fact @events
          => [[:advance 1 {:payload "a"} {:payload "b"} nil]])))

(deftest callback-test
  (binding [lifecycle/*in-progress?* false]
    (let [events (atom [])
          cb1 #(swap! events conj [:callback 1 %1 %2])
          prop-text-changed (fx/make-binding-prop
                              (fn bind-callback [^TextField text-field callback]
                                (swap! events conj [:bind])
                                (let [^ChangeListener listener (reify ChangeListener
                                                                 (changed [_ _ old new]
                                                                   (callback old new)))]
                                  (.addListener (.textProperty text-field) listener)
                                  #(do
                                     (swap! events conj [:unbind])
                                     (.removeListener (.textProperty text-field) listener))))
                              lifecycle/callback)
          ;; create => bind
          c (fx/create-component {:fx/type :text-field prop-text-changed cb1})
          ^TextField tf (fx/instance c)
          ;; change text => callback 1
          _ (.setText tf "a")
          cb2 #(swap! events conj [:callback 2 %1 %2])
          ;; callback lifecycle absorbs function changes
          c (fx/advance-component c {:fx/type :text-field prop-text-changed cb2})
          ;; change text => callback 2
          _ (.setText tf "b")
          ;; remove callback => unbind
          c (fx/advance-component c {:fx/type :text-field})
          ;; add callback again => bind
          c (fx/advance-component c {:fx/type :text-field prop-text-changed cb2})
          ;; change text => callback 2
          _ (.setText tf "c")]
      (fx/delete-component c)
      (is (= [[:bind]
              [:callback 1 "" "a"]
              [:callback 2 "a" "b"]
              [:unbind]
              [:bind]
              [:callback 2 "b" "c"]]
             @events)))))
