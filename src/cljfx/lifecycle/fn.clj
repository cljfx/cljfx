(ns cljfx.lifecycle.fn
  (:require [cljfx.component :as component]
            [cljfx.lifecycle :as lifecycle]))

(def component
  (with-meta {}
             {`lifecycle/create
              (fn [_ [f & args]]
                (let [ret (apply f args)]
                  (with-meta {:f f
                              :args args
                              :ret ret
                              :child (lifecycle/create-component ret)}
                             {`component/tag :f
                              `component/instance #(component/instance (:child %))})))

              `lifecycle/advance
              (fn [_ component [f & args]]
                (if (= args (:args component))
                  (update component :child lifecycle/advance-component (:ret component))
                  (let [ret (apply f args)]
                    (-> component
                        (assoc :ret ret :args args)
                        (update :child lifecycle/advance-component ret)))))

              `lifecycle/delete
              (fn [_ component]
                (lifecycle/delete-component (:child component)))}))