(ns cljfx.lifecycle
  (:require [cljfx.component :as component]))

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc] "Creates component or prop")
  (advance [this value new-desc] "Advances component or prop")
  (delete [this value] "Deletes component or prop"))

(defn create-component [desc]
  (create nil desc))

(defn advance-component [component desc]
  (advance nil component desc))

(defn delete-component [component]
  (delete nil component))

(def fn-component
  (with-meta {}
             {`create
              (fn [_ [f & args]]
                (let [ret (apply f args)]
                  (with-meta {:f f
                              :args args
                              :ret ret
                              :child (create-component ret)}
                             {`component/tag :f
                              `component/instance #(component/instance (:child %))})))

              `advance
              (fn [_ component [f & args]]
                (if (= args (:args component))
                  (update component :child advance-component (:ret component))
                  (let [ret (apply f args)]
                    (-> component
                        (assoc :ret ret :args args)
                        (update :child advance-component ret)))))

              `delete
              (fn [_ component]
                (delete-component (:child component)))}))