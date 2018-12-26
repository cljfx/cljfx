(ns cljfx.middleware
  (:require [cljfx.lifecycle :as lifecycle]))

(defn map-value [f]
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component (f value) opts))))

(defn map-opts [f]
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component value (f opts)))))

(defn- prepend-opt-value [f args opts]
  (apply vector f (::value opts) args))

(def ^:private exposed-value-fn-lifecycle
  (with-meta
    [::lifecycle/exposed-value-fn]
    {`lifecycle/create
     (fn [_ [f & args] opts]
       (lifecycle/create lifecycle/fn-dynamic-hiccup (prepend-opt-value f args opts) opts))

     `lifecycle/advance
     (fn [_ component [f & args] opts]
       (lifecycle/advance lifecycle/fn-dynamic-hiccup
                          component
                          (prepend-opt-value f args opts)
                          opts))

     `lifecycle/delete
     (fn [_ component opts]
       (lifecycle/delete lifecycle/fn-dynamic-hiccup component opts))}))

(defn fn-tag->exposed-lifecycle [tag]
  (when (fn? tag) exposed-value-fn-lifecycle))

(defn expose-value []
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component value (assoc opts ::value value)))))