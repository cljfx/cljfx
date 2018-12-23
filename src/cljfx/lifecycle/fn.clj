(ns cljfx.lifecycle.fn
  (:require [cljfx.component :as component]
            [cljfx.lifecycle :as lifecycle]))

(def component
  (with-meta [:cljfx.lifecycle/fn]
             {`lifecycle/create
              (fn [_ [f & args] opts]
                (let [ret (apply f args)]
                  (with-meta {:f f
                              :args args
                              :ret ret
                              :child (lifecycle/create-component ret opts)}
                             {`component/instance #(component/instance (:child %))})))

              `lifecycle/advance
              (fn [_ component [f & args] opts]
                (if (= args (:args component))
                  (update component :child lifecycle/advance-component (:ret component) opts)
                  (let [ret (apply f args)]
                    (-> component
                        (assoc :ret ret :args args)
                        (update :child lifecycle/advance-component ret opts)))))

              `lifecycle/delete
              (fn [_ component opts]
                (lifecycle/delete-component (:child component) opts))}))