(ns cljfx.prop
  "Part of a public API

  Shape of a prop config is internal and subject to change"
  (:require [cljfx.component :as component]
            [cljfx.mutator :as mutator]))

(defn make
  "Creates a prop config that describes how to manage a prop value and how to assign it

  `mutator` is a Mutator that will assign prop value to a mutable java object
  `lifecycle` is a Lifecycle for a prop value whose instance will be assigned whenever it
  changes
  Additional options:
  - `:coerce` (optional, default `identity`) - last mile value transformation before
    assigning it to a mutable java object
  - `:default` (optional) - default non-coerced value that will be assigned to a mutable
    java object when prop is removed from a prop map"
  [mutator lifecycle & {:keys [coerce default]
                        :or {coerce identity
                             default ::no-default}}]
  {:mutator (if (= default ::no-default)
              mutator
              (mutator/wrap-default mutator default))
   :lifecycle lifecycle
   :coerce coerce})

(defn lifecycle [prop]
  (:lifecycle prop))

(defn coerce [prop component]
  ((:coerce prop) (component/instance component)))

(defn assign! [prop instance component]
  (let [{:keys [mutator coerce]} prop]
    (mutator/assign! mutator instance coerce (component/instance component))))

(defn replace! [prop instance old-component new-component]
  (let [{:keys [mutator coerce]} prop]
    (mutator/replace! mutator
                      instance
                      coerce
                      (component/instance old-component)
                      (component/instance new-component))))

(defn retract! [prop instance component]
  (let [{:keys [mutator coerce]} prop]
    (mutator/retract! mutator instance coerce (component/instance component))))
