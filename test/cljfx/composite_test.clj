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
  (let [state-m (mk-state {})
        {:keys [props-config grab-history state]} (mk-props [:a :b :c] state-m)
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
                => [;;FIXME
                    ;{:op :set-prop, :prop :a, :v nil}
                    ;{:op :set-prop, :prop :b, :v nil}
                    ;{:op :set-prop, :prop :c, :v nil}
                    ])
        _ (fact (component/instance component)
                => nil)
        ]))
