(ns cljfx.prop
  (:require [cljfx.component :as component]
            [cljfx.mutator :as mutator]))

(defn make [mutator lifecycle & {:keys [coerce default]
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

;; TODO remove

(defn extract-single [args]
  (if-not (= 1 (count args))
    (throw (ex-info "Should have exactly one arg"
                    {:args args})))
  (first args))

(defn extract-all [args]
  args)