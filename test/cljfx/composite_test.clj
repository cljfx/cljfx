(ns cljfx.composite-test
  (:require [clojure.test :refer :all]
            [testit.core :refer :all]
            [cljfx.test-helpers :refer :all]
            [cljfx.component :as component]
            [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]))

(deftest composite-lifecycle-test
  (let [{:keys [props-config grab-history state]} (mk-props [:a :b :c])
        prop-order {:a 0, :b 1, :c 2}
        lifecycle (composite/lifecycle
                    {:ctor (fn [& args]
                             (let [[{:keys [next-instance]}]
                                   (swap-vals! state
                                               #(-> %
                                                    (dissoc :next-instance)
                                                    (update :history conj
                                                            {:op :ctor
                                                             :args (vec args)})))]
                               (or next-instance :obj)))
                     :args [:a]
                     :props props-config
                     :prop-order prop-order})

        ;; pass :a to ctor, assign :b
        component (lifecycle/create
                    lifecycle
                    {:fx/type lifecycle
                     :a 42
                     :b 24}
                    {::foo 1})
        _ (fact (grab-history)
                => [{:op :ctor, :args ["42"]}
                    {:op :assign!, :prop :b, :instance :obj, :coerced-value "24", :value 24}])
        _ (fact (component/instance component)
                => :obj)

        ;; update :a, retract :b
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :a 2}
                    {::foo nil})
        _ (fact (grab-history)
                => [{:op :replace!, :prop :a, :instance :obj, :coerced-new-value "2", :old-value 42, :new-value 2}
                    {:op :retract!, :prop :b, :instance :obj, :coerced-value "24", :value 24}])
        _ (fact (component/instance component)
                => :obj)

        ;; replace :a, assign :b/:c
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :a 1
                     :b 2
                     :c 3}
                    nil)
        _ (fact (grab-history)
                => [{:op :replace!, :prop :a, :instance :obj, :coerced-new-value "1", :old-value 2, :new-value 1}
                    {:op :assign!, :prop :b, :instance :obj, :coerced-value "2", :value 2}
                    {:op :assign!, :prop :c, :instance :obj, :coerced-value "3", :value 3}])
        _ (fact (component/instance component)
                => :obj)

        ;; replace :a/:b/:c
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :a 1
                     :b 2
                     :c 3}
                    nil)
        _ (fact (grab-history)
                => [{:op :replace!, :prop :a, :instance :obj, :coerced-new-value "1", :old-value 1, :new-value 1}
                    {:op :replace!, :prop :b, :instance :obj, :coerced-new-value "2", :old-value 2, :new-value 2}
                    {:op :replace!, :prop :c, :instance :obj, :coerced-new-value "3", :old-value 3, :new-value 3}])
        _ (fact (component/instance component)
                => :obj)

        ;; replace :a/:b/:c
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :a 1
                     :b 2
                     :c 3}
                    nil)
        _ (fact (grab-history)
                => [{:op :replace!, :prop :a, :instance :obj, :coerced-new-value "1", :old-value 1, :new-value 1}
                    {:op :replace!, :prop :b, :instance :obj, :coerced-new-value "2", :old-value 2, :new-value 2}
                    {:op :replace!, :prop :c, :instance :obj, :coerced-new-value "3", :old-value 3, :new-value 3}])
        _ (fact (component/instance component)
                => :obj)

        ;; delete
        component (lifecycle/delete
                    lifecycle
                    component
                    nil)
        _ (fact (grab-history)
                => [])
        _ (fact (component/instance component)
                => nil)]))

(deftest unordered-composite-lifecycle-preserves-nil-valued-props-test
  (let [{:keys [props-config grab-history]} (mk-props [:a :b])
        lifecycle (composite/lifecycle
                    {:ctor (fn [] :obj)
                     :args []
                     :props props-config})
        component (lifecycle/create
                    lifecycle
                    {:fx/type lifecycle
                     :a nil}
                    nil)
        _ (fact (grab-history)
                => [{:op :assign!, :prop :a, :instance :obj, :coerced-value "nil", :value nil}])
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :a nil}
                    nil)
        _ (fact (grab-history)
                => [{:op :replace!, :prop :a, :instance :obj, :coerced-new-value "nil", :old-value nil, :new-value nil}])
        component (lifecycle/advance
                    lifecycle
                    component
                    {:fx/type lifecycle
                     :b nil}
                    nil)]
    (fact (set (grab-history))
          => #{{:op :assign!, :prop :b, :instance :obj, :coerced-value "nil", :value nil}
               {:op :retract!, :prop :a, :instance :obj, :coerced-value "nil", :value nil}})
    (fact (component/instance component)
          => :obj)))

;; Concrete composites reuse stable components and retain advanced prop state.
;; Cleanup must see the latest state even when descriptions remain equal.
(deftest composite-component-state-test
  (testing "Unchanged props reuse the component and native instance"
    (let [definition
          (composite/lifecycle {:ctor (fn make-instance [] (Object.))
                               :args []
                               :props {}})

          initial (lifecycle/create definition {} {})
          advanced (lifecycle/advance definition initial {} {})]
      (is (identical? initial advanced))
      (is (identical? (component/instance initial) (component/instance advanced)))
      (lifecycle/delete definition advanced {})))
  (testing "Equal descriptions still advance prop lifecycles and retain their latest state"
    (let [deleted (atom [])

          value-lifecycle
          (reify lifecycle/Lifecycle
            (create [_ desc _]
              (with-meta {:generation 0 :value desc} {`component/instance :value}))
            (advance [_ value desc _]
              (assoc value :generation (inc (:generation value)) :value desc))
            (delete [_ value _]
              (swap! deleted conj (:generation value))))

          definition
          (composite/lifecycle
            {:ctor (fn make-target [] (atom nil))
             :args []
             :props {:value (prop/make (mutator/setter reset!) value-lifecycle)}})

          initial (lifecycle/create definition {:value false} {})
          advanced (lifecycle/advance definition initial {:value false} {})
          target (component/instance initial)]
      (is (false? @target))
      (is (identical? target (component/instance advanced)))
      (is (not (identical? initial advanced)))
      (lifecycle/delete definition advanced {})
      (is (= [1] @deleted)))))
