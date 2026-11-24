(ns cljfx.lifecycle-test
  (:require [cljfx.api :as fx]
            [cljfx.component :as component]
            [cljfx.context :as context]
            [cljfx.fx.border-pane :as fx.border-pane]
            [cljfx.fx.label :as fx.label]
            [cljfx.fx.tab :as fx.tab]
            [cljfx.fx.tab-pane :as fx.tab-pane]
            [cljfx.fx.text-field :as fx.text-field]
            [cljfx.fx.v-box :as fx.v-box]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]
            [cljfx.test-helpers :refer :all]
            [clojure.test :refer :all]
            [testit.core :refer :all])
  (:import [clojure.lang IRef]
           [javafx.beans.value ChangeListener]
           [javafx.geometry Insets Pos]
           [javafx.scene.control Label Tab TabPane TextField]
           [javafx.scene.layout BorderPane Priority VBox]))

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

(deftest wrap-many-preserves-component-on-noop-advance-test
  (let [events (atom [])
        child-lifecycle (reify lifecycle/Lifecycle
                          (create [_ desc opts]
                            (swap! events conj [:create desc opts])
                            desc)
                          (advance [_ component desc opts]
                            (swap! events conj [:advance component desc opts])
                            (if (= component desc) component desc))
                          (delete [_ component opts]
                            (swap! events conj [:delete component opts])))
        lifecycle (lifecycle/wrap-many child-lifecycle identity identity)
        desc [:a :b :c]
        component-1 (lifecycle/create lifecycle desc {::foo 1})
        instance-1 (component/instance component-1)
        _ (reset! events [])
        component-2 (lifecycle/advance lifecycle component-1 desc {::foo 2})]
    (fact @events
          => [[:advance :a :a {::foo 2}]
              [:advance :b :b {::foo 2}]
              [:advance :c :c {::foo 2}]])
    (fact (identical? component-1 component-2)
          => true)
    (fact (identical? instance-1 (component/instance component-2))
          => true)
    (reset! events [])
    (let [component-3 (lifecycle/advance lifecycle component-2 [:a :c] {::foo 3})]
      (fact @events
            => [[:advance :a :a {::foo 3}]
                [:advance :c :c {::foo 3}]
                [:delete :b {::foo 3}]])
      (fact (component/instance component-3)
            => [:a :c])
      (reset! events [])
      (let [component-4 (lifecycle/advance lifecycle component-3 desc {::foo 4})]
        (fact @events
              => [[:advance :a :a {::foo 4}]
                  [:create :b {::foo 4}]
                  [:advance :c :c {::foo 4}]])
        (fact (component/instance component-4)
              => desc)))))

(deftest wrap-many-reuses-instance-when-only-child-components-change-test
  (let [child-lifecycle (reify lifecycle/Lifecycle
                          (create [_ {:keys [instance value]} _]
                            (with-meta
                              {:instance instance
                               :value value}
                              {`component/instance :instance}))
                          (advance [_ component {:keys [instance value]} _]
                            (assoc component :instance instance :value value))
                          (delete [_ _ _]))
        lifecycle (lifecycle/wrap-many child-lifecycle :key #(dissoc % :key))
        instance-a (Object.)
        instance-b (Object.)
        component-1 (lifecycle/create
                      lifecycle
                      [{:key :a :instance instance-a :value 1}
                       {:key :b :instance instance-b :value 2}]
                      nil)
        instance-1 (component/instance component-1)
        component-2 (lifecycle/advance
                      lifecycle
                      component-1
                      [{:key :a :instance instance-a :value 10}
                       {:key :b :instance instance-b :value 20}]
                      nil)]
    (fact (identical? component-1 component-2)
          => false)
    (fact (identical? instance-1 (component/instance component-2))
          => true)))

(deftest wrap-many-supports-ordered-seqable-inputs-test
  (let [events (atom [])
        child-lifecycle (reify lifecycle/Lifecycle
                          (create [_ desc opts]
                            (swap! events conj [:create desc opts])
                            desc)
                          (advance [_ component desc opts]
                            (swap! events conj [:advance component desc opts])
                            (if (= component desc) component desc))
                          (delete [_ component opts]
                            (swap! events conj [:delete component opts])))
        lifecycle (lifecycle/wrap-many child-lifecycle identity identity)
        component-1 (lifecycle/create lifecycle [:a :b :c] nil)
        _ (reset! events [])
        component-2 (lifecycle/advance lifecycle component-1 '(:a :b :c) {::foo 1})]
    (fact @events
          => [[:advance :a :a {::foo 1}]
              [:advance :b :b {::foo 1}]
              [:advance :c :c {::foo 1}]])
    (fact (identical? component-1 component-2)
          => true)
    (reset! events [])
    (let [desc (for [x [:a :b :c]] x)
          component-3 (lifecycle/advance lifecycle component-2 desc {::foo 2})]
      (fact @events
            => [[:advance :a :a {::foo 2}]
                [:advance :b :b {::foo 2}]
                [:advance :c :c {::foo 2}]])
      (fact (identical? component-2 component-3)
            => true)
      (fact (identical? (component/instance component-2)
                        (component/instance component-3))
            => true))))

(deftest wrap-many-handles-empty-and-nil-transitions-test
  (let [events (atom [])
        lifecycle (lifecycle/wrap-many
                   (tracking-lifecycle events)
                   identity
                   identity)
        component-1 (lifecycle/create lifecycle nil {::foo 1})
        component-2 (lifecycle/advance lifecycle component-1 [] {::foo 2})]
    (fact (component/instance component-1)
          => [])
    (fact @events
          => [])
    (fact (identical? component-1 component-2)
          => true)
    (let [component-3 (lifecycle/advance lifecycle component-2 [:a :b] {::foo 3})]
      (fact (component/instance component-3)
            => [1 2])
      (fact @events
            => [[:create 1 :a {::foo 3}]
                [:create 2 :b {::foo 3}]])
      (reset! events [])
      (let [component-4 (lifecycle/advance lifecycle component-3 nil {::foo 4})]
        (fact (component/instance component-4)
              => [])
        (fact (set @events)
              => #{[:delete 1 {::foo 4}]
                   [:delete 2 {::foo 4}]})
        (reset! events [])
        (let [component-5 (lifecycle/advance lifecycle component-4 nil {::foo 5})]
          (fact @events
                => [])
          (fact (identical? component-4 component-5)
                => true))))))

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

(deftest wrap-many-distinguishes-nil-and-missing-keys-test
  (let [events (atom [])
        lifecycle (lifecycle/wrap-many (tracking-lifecycle events))
        component (lifecycle/create
                   lifecycle
                   [{:fx/key nil :v 1}
                    {:v 2}
                    {:fx/key nil :v 3}
                    {:v 4}]
                   nil)
        _ (reset! events [])
        component (lifecycle/advance
                   lifecycle
                   component
                   [{:v 20}
                    {:fx/key nil :v 10}
                    {:v 40}
                    {:fx/key nil :v 30}]
                   nil)]
    (fact (component/instance component)
          => [2 1 4 3])
    (fact (set @events)
          => #{[:advance 1 {:v 1} {:v 10} nil]
               [:advance 2 {:v 2} {:v 20} nil]
               [:advance 3 {:v 3} {:v 30} nil]
               [:advance 4 {:v 4} {:v 40} nil]})))

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

(deftest wrap-many-preserves-clojure-equivalent-keys-test
  (let [events (atom [])
        lifecycle (lifecycle/wrap-many
                    (tracking-lifecycle events)
                    :id
                    #(select-keys % [:payload]))
        component (lifecycle/create lifecycle [{:id 1M :payload "a"}] nil)
        _ (reset! events [])
        component (lifecycle/advance lifecycle component [{:id 1.0M :payload "b"}] nil)]
    (fact (component/instance component)
          => [1])
    (fact @events
          => [[:advance 1 {:payload "a"} {:payload "b"} nil]])
    (reset! events [])
    (lifecycle/delete lifecycle component nil)
    (fact @events
          => [[:delete 1 nil]])))

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

;; Nil descriptions represent absence without suppressing invalid descriptions
;; or discarding the state of a component whose function returns nil.
(deftest dynamic-nil-description-test
  (testing "Nil bypasses type resolution and reuses an already absent component"
    (let [resolved-types (atom [])

          opts
          {:fx.opt/type->lifecycle
           (fn resolve-type [type]
             (swap! resolved-types conj type)
             type)}

          child (lifecycle/create lifecycle/dynamic nil opts)]
      (try
        (is (nil? (component/instance child)))
        (is (identical? child (lifecycle/advance lifecycle/dynamic child nil opts)))
        (is (= [] @resolved-types))
        (finally
          (lifecycle/delete lifecycle/dynamic child opts)))))
  (testing "Transitioning to nil deletes the old child; restoring creates a new one"
    (let [events (atom [])
          child-lifecycle (tracking-lifecycle events)
          desc {:fx/type child-lifecycle}
          current (volatile! (fx/create-component nil))]
      (try
        (vswap! current fx/advance-component desc)
        (let [first-component @current]
          (is (= 1 (fx/instance first-component)))
          (vswap! current fx/advance-component nil)
          (is (nil? (fx/instance @current)))
          (is (= 1 (fx/instance first-component)))
          (is (= [[:create 1] [:delete 1]]
                 (mapv #(subvec % 0 2) @events))))
        (vswap! current fx/advance-component desc)
        (is (= 2 (fx/instance @current)))
        (finally
          (fx/delete-component @current)))
      (is (= [[:create 1] [:delete 1] [:create 2] [:delete 2]]
             (mapv #(subvec % 0 2) @events)))))
  (testing "An explicit nil child clears a native property"
    @(fx/on-fx-thread
       (let [desc
             {:fx/type fx.tab/lifecycle
              :content {:fx/type fx.label/lifecycle :text "Content"}}

             current (volatile! (fx/create-component desc))
             ^Tab tab (fx/instance @current)]
         (try
           (is (= "Content" (.getText ^Label (.getContent tab))))
           (vswap! current fx/advance-component (assoc desc :content nil))
           (is (identical? tab (fx/instance @current)))
           (is (nil? (.getContent tab)))
           (vswap! current fx/advance-component desc)
           (is (= "Content" (.getText ^Label (.getContent tab))))
           (finally
             (fx/delete-component @current))))))
  (testing "A function can return nil while ext-state stays mounted"
    (let [current (atom nil)
          update-state (atom nil)

          view
          (fn optional-view [{:keys [visible state swap-state]}]
            (reset! update-state swap-state)
            (when visible
              {:fx/type fx.label/lifecycle :text (str state)}))

          desc
          (fn describe-stateful-view [visible]
            {:fx/type fx.tab/lifecycle
             :content {:fx/type fx/ext-state
                       :initial-state 0
                       :desc {:fx/type view :visible visible}}})]
      (try
        (reset! current @(fx/on-fx-thread (fx/create-component (desc false))))
        (let [^Tab tab (fx/instance @current)
              updater @update-state]
          (is (nil? (.getContent tab)))
          (updater inc)
          @(fx/run-later)
          (is (nil? (.getContent tab)))
          (reset! current @(fx/on-fx-thread
                             (fx/advance-component @current (desc true))))
          (is (= "1" (.getText ^Label (.getContent tab))))
          (reset! current @(fx/on-fx-thread
                             (fx/advance-component @current (desc false))))
          (is (nil? (.getContent tab)))
          (updater inc)
          @(fx/run-later)
          (reset! current @(fx/on-fx-thread
                             (fx/advance-component @current (desc true))))
          (is (= "2" (.getText ^Label (.getContent tab))))
          (is (identical? updater @update-state)))
        (finally
          (when @current
            @(fx/on-fx-thread (fx/delete-component @current)))))))
  (testing "Malformed nonnil descriptions still fail during creation and advancement"
    (let [child (fx/create-component nil)]
      (try
        (doseq [desc [{} {:fx/type nil} {:fx/type ::unknown} false]]
          (is (thrown? IllegalArgumentException (fx/create-component desc)))
          (is (thrown? IllegalArgumentException (fx/advance-component child desc))))
        (is (thrown? IllegalArgumentException
                     (fx/create-component {:fx/type (fn invalid-view [_] {})})))
        (finally
          (fx/delete-component child)))))
  (testing "Exceptions from a function component propagate unchanged"
    (let [failure (ex-info "View failed" {})

          view
          (fn broken-view [_] (throw failure))]
      (is (identical? failure
                      (try
                        (fx/create-component {:fx/type view})
                        (catch Exception caught caught)))))))

;; Extra-prop resources belong to a nonnil target, while the child component
;; survives target changes and absence in both wrapper APIs.
(deftest extra-props-target-lifetime-test
  (doseq [style [:inline :detached]]
    (testing (name style)
      (let [events (atom [])
            next-id (atom 0)
            child-created (atom 0)
            child-deleted (atom [])
            forwarded (atom [])
            target-a (atom {:name :a})
            target-b (atom {:name :b})
            opts {::option :passed}

            child-lifecycle
            (reify lifecycle/Lifecycle
              (create [_ desc opts]
                (swap! child-created inc)
                (swap! forwarded conj [(dissoc desc :target) opts])
                (with-meta {:instance (:target desc) :generation 0}
                  {`component/instance :instance}))
              (advance [_ child desc opts]
                (swap! forwarded conj [(dissoc desc :target) opts])
                (assoc child :instance (:target desc)
                       :generation (inc (:generation child))))
              (delete [_ child _]
                (swap! child-deleted conj (:generation child))))

            value-lifecycle
            (reify lifecycle/Lifecycle
              (create [_ desc _]
                (let [id (swap! next-id inc)]
                  (swap! events conj [:create id desc])
                  {:id id :value desc}))
              (advance [_ value desc _]
                (swap! events conj [:advance (:id value) (:value value) desc])
                (assoc value :value desc))
              (delete [_ value _]
                (swap! events conj [:delete (:id value) (:value value)])))

            extra-prop
            (prop/make
              (mutator/setter
                (fn assign-extra! [target value]
                  (swap! events conj [:assign (:name @target) value])
                  (swap! target assoc :extra value)))
              value-lifecycle
              :coerce (fn coerce-extra [{:keys [id value]}]
                        (swap! events conj [:coerce id value])
                        [:applied id value]))

            wrapper
            (case style
              :inline (lifecycle/wrap-extra-props child-lifecycle {:extra extra-prop})
              :detached (lifecycle/make-ext-with-props child-lifecycle {:extra extra-prop}))

            describe
            (fn describe-wrapper [target props]
              (case style
                :inline (assoc props :target target :forwarded :child)
                :detached {:desc {:target target :forwarded :child} :props props}))

            current
            (volatile! (lifecycle/create wrapper (describe nil {:extra :initial}) opts))]
        (try
          (is (nil? (component/instance @current)))
          (is (= [] @events))
          (vswap! current #(lifecycle/advance wrapper % (describe nil {:extra :hidden}) opts))
          (is (= [] @events))
          (vswap! current #(lifecycle/advance wrapper % (describe target-a {:extra :first}) opts))
          (is (= [:applied 1 :first] (:extra @target-a)))
          (vswap! current #(lifecycle/advance wrapper % (describe target-a {:extra :first}) opts))
          (is (= [:advance 1 :first :first] (peek @events)))
          (vswap! current #(lifecycle/advance wrapper % (describe target-a {:extra :updated}) opts))
          (is (= [:applied 1 :updated] (:extra @target-a)))
          (vswap! current #(lifecycle/advance wrapper % (describe target-b {:extra :next}) opts))
          (is (= [:applied 2 :next] (:extra @target-b)))
          (vswap! current #(lifecycle/advance wrapper % (describe target-b {}) opts))
          (is (nil? (:extra @target-b)))
          (vswap! current #(lifecycle/advance wrapper % (describe target-b {:extra nil}) opts))
          (is (= [:applied 3 nil] (:extra @target-b)))
          (vswap! current #(lifecycle/advance wrapper % (describe nil {:extra :unmounted}) opts))
          (is (nil? (component/instance @current)))
          (vswap! current #(lifecycle/advance wrapper % (describe nil {:extra :new-hidden}) opts))
          (is (= 3 @next-id))
          (vswap! current #(lifecycle/advance wrapper % (describe target-a {:extra :restored}) opts))
          (is (= [:applied 4 :restored] (:extra @target-a)))
          (finally
            (lifecycle/delete wrapper @current opts)))
        (is (= [[:create 1 :first] [:coerce 1 :first] [:assign :a [:applied 1 :first]]
                [:advance 1 :first :first]
                [:advance 1 :first :updated] [:coerce 1 :updated] [:assign :a [:applied 1 :updated]]
                [:delete 1 :updated]
                [:create 2 :next] [:coerce 2 :next] [:assign :b [:applied 2 :next]]
                [:assign :b nil] [:delete 2 :next]
                [:create 3 nil] [:coerce 3 nil] [:assign :b [:applied 3 nil]]
                [:delete 3 nil]
                [:create 4 :restored] [:coerce 4 :restored] [:assign :a [:applied 4 :restored]]
                [:delete 4 :restored]]
               @events))
        (is (= 1 @child-created))
        (is (= [10] @child-deleted))
        (is (= (vec (repeat 11 [{:forwarded :child} opts])) @forwarded))
        (let [event-count (count @events)

              absent
              (lifecycle/create wrapper (describe nil {:extra :unused}) opts)]
          (lifecycle/delete wrapper absent opts)
          (is (= event-count (count @events))))))))

;; Native constraints are assigned only to present nodes, including recreated
;; roots, without resetting the surrounding ext-state component.
(deftest extra-props-native-constraints-test
  (let [current (atom nil)
        update-state (atom nil)

        view
        (fn constrained-view [{:keys [kind state swap-state]}]
          (reset! update-state swap-state)
          (when-not (= :none kind)
            {:fx/type (case kind
                        :label fx.label/lifecycle
                        :text-field fx.text-field/lifecycle)
             :text (str state)}))

        describe
        (fn describe-pane [kind margin]
          {:fx/type fx.border-pane/lifecycle
           :center {:fx/type fx/ext-state
                    :initial-state 0
                    :border-pane/margin margin
                    :border-pane/alignment :bottom-right
                    :desc {:fx/type view :kind kind}}})]
    (try
      (reset! current @(fx/on-fx-thread (fx/create-component (describe :none 10))))
      (let [^BorderPane pane (fx/instance @current)
            updater @update-state]
        (is (nil? (.getCenter pane)))
        (updater inc)
        @(fx/run-later)
        (reset! current @(fx/on-fx-thread
                           (fx/advance-component @current (describe :label 10))))
        (is (= "1" (.getText ^Label (.getCenter pane))))
        (is (= (Insets. 10.0) (BorderPane/getMargin (.getCenter pane))))
        (is (= Pos/BOTTOM_RIGHT (BorderPane/getAlignment (.getCenter pane))))
        (reset! current @(fx/on-fx-thread
                           (fx/advance-component @current (describe :text-field 20))))
        (is (= "1" (.getText ^TextField (.getCenter pane))))
        (is (= (Insets. 20.0) (BorderPane/getMargin (.getCenter pane))))
        (is (= Pos/BOTTOM_RIGHT (BorderPane/getAlignment (.getCenter pane))))
        (reset! current @(fx/on-fx-thread
                           (fx/advance-component @current (describe :none 30))))
        (is (nil? (.getCenter pane)))
        (updater inc)
        @(fx/run-later)
        (reset! current @(fx/on-fx-thread
                           (fx/advance-component @current (describe :label 30))))
        (is (= "2" (.getText ^Label (.getCenter pane))))
        (is (= (Insets. 30.0) (BorderPane/getMargin (.getCenter pane))))
        (is (identical? updater @update-state))
        (let [label (.getCenter pane)

              without-constraints
              (update (describe :label 30) :center
                      dissoc :border-pane/margin :border-pane/alignment)]
          (reset! current @(fx/on-fx-thread
                             (fx/advance-component @current without-constraints)))
          (is (identical? label (.getCenter pane)))
          (is (nil? (BorderPane/getMargin (.getCenter pane))))
          (is (nil? (BorderPane/getAlignment (.getCenter pane))))))
      (finally
        (when @current
          @(fx/on-fx-thread (fx/delete-component @current)))))))

;; Compacted instance positions stay independent from description positions
;; for every transition between four visible/absent children, including false.
(deftest wrap-many-nil-position-transitions-test
  (let [instances [(Object.) false (Object.) (Object.)]
        wrapped (lifecycle/wrap-many lifecycle/scalar :key :instance)

        describe
        (fn describe-mask [mask]
          (mapv (fn [key instance]
                  {:key key :instance (when (bit-test mask key) instance)})
                (range 4) instances))]
    (doseq [before (range 16)
            after (range 16)]
      (let [before-desc (describe before)
            after-desc (describe after)
            first-component (lifecycle/create wrapped before-desc {})
            first-instance (component/instance first-component)
            current (volatile! first-component)]
        (try
          (is (= (into [] (keep :instance) before-desc) first-instance))
          (vswap! current #(lifecycle/advance wrapped % after-desc {}))
          (is (= (into [] (keep :instance) after-desc) (component/instance @current)))
          (is (= (into [] (keep :instance) before-desc) (component/instance first-component)))
          (when (= before after)
            (is (identical? first-component @current))
            (is (identical? first-instance (component/instance @current))))
          (finally
            (lifecycle/delete wrapped @current {})))))))

(defn- instance-tracking-lifecycle [events]
  (let [next-id (atom 0)]
    (reify lifecycle/Lifecycle
      (create [_ desc _]
        (let [id (swap! next-id inc)]
          (swap! events conj [:create id])
          (with-meta {:id id :instance (:instance desc) :revision (:revision desc)}
            {`component/instance :instance})))
      (advance [_ child desc _]
        (swap! events conj [:advance (:id child)])
        (assoc child :instance (:instance desc) :revision (:revision desc)))
      (delete [_ child _]
        (swap! events conj [:delete (:id child)])))))

;; Nil instances do not unmount keyed components or consume visible positions;
;; hidden updates and removal preserve unchanged exposed vectors.
(deftest wrap-many-nil-ownership-test
  (let [events (atom [])
        wrapped (lifecycle/wrap-many (instance-tracking-lifecycle events))

        desc
        [{:fx/key :a :instance :a}
         {:fx/key :b :instance nil}
         {:fx/key :c :instance :c}]

        current (volatile! (lifecycle/create wrapped desc {}))
        first-component @current]
    (try
      (is (= [:a :c] (component/instance @current)))
      (vswap! current #(lifecycle/advance wrapped % desc {}))
      (is (identical? first-component @current))
      (vswap! current #(lifecycle/advance wrapped % (assoc-in desc [1 :instance] :b) {}))
      (is (= [:a :b :c] (component/instance @current)))
      (vswap! current #(lifecycle/advance wrapped % desc {}))
      (let [hidden-component @current
            hidden-instance (component/instance @current)
            changed (assoc-in desc [1 :revision] 1)]
        (vswap! current #(lifecycle/advance wrapped % changed {}))
        (is (not (identical? hidden-component @current)))
        (is (identical? hidden-instance (component/instance @current)))
        (reset! events [])
        (vswap! current #(lifecycle/advance wrapped % [(nth changed 2) (nth changed 1) (nth changed 0)] {}))
        (is (= [:advance :advance :advance] (mapv first @events)))
        (is (= [3 2 1] (mapv second @events)))
        (is (= [:c :a] (component/instance @current)))
        (let [visible-instance (component/instance @current)]
          (vswap! current #(lifecycle/advance wrapped % [(nth changed 2) (nth changed 0)] {}))
          (is (identical? visible-instance (component/instance @current)))))
      (finally
        (lifecycle/delete wrapped @current {})))
    (is (= {1 1, 2 1, 3 1}
           (frequencies (into [] (keep (fn [[op id]] (when (= :delete op) id))) @events))))))

;; Absent children still count toward duplicate, explicit-nil, and unkeyed key
;; occurrences, so reordering does not recreate the wrong components.
(deftest wrap-many-nil-key-occurrences-test
  (let [events (atom [])
        wrapped (lifecycle/wrap-many (instance-tracking-lifecycle events))

        desc
        [{:fx/key :duplicate :instance nil}
         {:fx/key :duplicate :instance :b}
         {:fx/key nil :instance nil}
         {:instance :c}
         {:fx/key nil :instance :a}
         {:instance nil}]

        current (volatile! (lifecycle/create wrapped desc {}))]
    (try
      (is (= [:b :c :a] (component/instance @current)))
      (is (= (mapv #(vector :create %) (range 1 7)) @events))
      (reset! events [])
      (vswap! current #(lifecycle/advance wrapped %
                                          [{:fx/key nil :instance nil}
                                           {:instance :c}
                                           {:fx/key :duplicate :instance :b}
                                           {:fx/key :duplicate :instance nil}
                                           {:instance nil}
                                           {:fx/key nil :instance :a}]
                                          {}))
      (is (= [:c :b :a] (component/instance @current)))
      (is (= (mapv #(vector :advance %) [3 4 1 2 6 5]) @events))
      (finally
        (lifecycle/delete wrapped @current {})))
    (is (= (zipmap (range 1 7) (repeat 1))
           (frequencies (into [] (keep (fn [[op id]] (when (= :delete op) id))) @events))))))

;; Native child lists omit nil instances while retaining state, constraints,
;; sibling controls, and subscriptions until their descriptions are removed.
(deftest nil-instance-native-collections-test
  (testing "VBox retains state and constraints across disappearing children"
    (let [current (atom nil)
          ^IRef watched (atom nil)
          update-state (atom nil)

          view
          (fn optional-child [{:keys [visible state swap-state]}]
            (reset! update-state swap-state)
            (when visible {:fx/type fx.label/lifecycle :text (str state)}))

          describe
          (fn describe-children [visible]
            {:fx/type fx.v-box/lifecycle
             :children [nil
                        {:fx/type fx/ext-watcher
                         :fx/key :optional
                         :ref watched
                         :v-box/vgrow :always
                         :desc {:fx/type fx/ext-state
                                :initial-state 0
                                :desc {:fx/type view :visible visible}}}
                        {:fx/type fx.label/lifecycle :fx/key :fixed :text "Fixed"}]})

          advance!
          (fn advance-children! [desc]
            (reset! current @(fx/on-fx-thread (fx/advance-component @current desc))))]
      (try
        (reset! current @(fx/on-fx-thread (fx/create-component (describe false))))
        (let [^VBox box (fx/instance @current)
              children (.getChildren box)
              fixed (.get children 0)
              updater @update-state]
          (is (= [fixed] (vec children)))
          (is (= 1 (count (.getWatches watched))))
          (updater inc)
          @(fx/run-later)
          (advance! (describe true))
          (is (= ["1" "Fixed"] (mapv #(.getText ^Label %) children)))
          (is (= Priority/ALWAYS (VBox/getVgrow (.get children 0))))
          (is (identical? fixed (.get children 1)))
          (advance! (describe false))
          (is (= [fixed] (vec children)))
          (is (= 1 (count (.getWatches watched))))
          (updater inc)
          @(fx/run-later)
          (advance! (describe true))
          (is (= ["2" "Fixed"] (mapv #(.getText ^Label %) children)))
          (is (identical? updater @update-state))
          (is (identical? fixed (.get children 1)))
          (advance! (assoc (describe false) :children
                           [nil {:fx/type fx.label/lifecycle :fx/key :fixed :text "Fixed"}]))
          (is (= [fixed] (vec children)))
          (is (= {} (.getWatches watched)))
          (advance! (assoc (describe false) :children [nil]))
          (is (= [] (vec children)))
          (advance! (describe true))
          (is (= ["0" "Fixed"] (mapv #(.getText ^Label %) children))))
        (finally
          (when @current
            @(fx/on-fx-thread (fx/delete-component @current)))))
      (is (= {} (.getWatches watched)))))
  (testing "TabPane filters instances produced by lifecycle/dynamics"
    @(fx/on-fx-thread
       (let [optional
             (fn optional-tab [{:keys [visible]}]
               (when visible {:fx/type fx.tab/lifecycle :text "Optional"}))

             describe
             (fn describe-tabs [visible]
               {:fx/type fx.tab-pane/lifecycle
                :tabs [nil
                       {:fx/type optional :fx/key :optional :visible visible}
                       {:fx/type fx.tab/lifecycle :fx/key :fixed :text "Fixed"}]})

             current (volatile! (fx/create-component (describe false)))
             ^TabPane tabs (fx/instance @current)
             fixed (first (.getTabs tabs))]
         (try
           (is (= [fixed] (vec (.getTabs tabs))))
           (vswap! current fx/advance-component (describe true))
           (is (= ["Optional" "Fixed"] (mapv #(.getText ^Tab %) (.getTabs tabs))))
           (is (identical? fixed (second (.getTabs tabs))))
           (vswap! current fx/advance-component (describe false))
           (is (= [fixed] (vec (.getTabs tabs))))
           (finally
             (fx/delete-component @current))))))
  (testing "Ext-many exposes no nil instances"
    (let [desc
          {:fx/type fx/ext-many
           :desc [nil
                  {:fx/type fx/ext-instance-factory
                   :create (fn absent-instance [] nil)}
                  nil]}

          current (volatile! (fx/create-component desc))]
      (try
        (is (= [] (fx/instance @current)))
        (vswap! current fx/advance-component desc)
        (is (= [] (fx/instance @current)))
        (finally
          (fx/delete-component @current))))))
