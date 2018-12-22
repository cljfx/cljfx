(ns cljfx.middleware
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.lifecycle.fn :as lifecycle.fn]
            [cljfx.impl :as impl]))

(defn map-value [f]
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component (f value) opts))))

(defn map-opts [f]
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component value (f opts)))))

(defn add-map-event-handler [handler]
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component value (update opts
                                         :cljfx.opt/map-event-handler
                                         (fn [f]
                                           (if (some? f)
                                             (fn [e]
                                               (f e)
                                               (handler e))
                                             handler)))))))

(defn add-tag->component-lifecycle-fn [existing f]
  (if (some? existing)
    (fn [tag]
      (or (existing tag)
          (f tag)))
    f))

(defn- prepend-opt-value [f args]
  (apply vector f (::value impl/*opts*) args))

(def ^:private exposed-value-fn-lifecycle
  (with-meta
    [::lifecycle/exposed-value-fn]
    {`lifecycle/create
     (fn [_ [f & args]]
       (lifecycle/create lifecycle.fn/component (prepend-opt-value f args)))

     `lifecycle/advance
     (fn [_ component [f & args]]
       (lifecycle/advance lifecycle.fn/component component (prepend-opt-value f args)))

     `lifecycle/delete
     (fn [_ component]
       (lifecycle/delete lifecycle.fn/component component))}))

(defn expose-value []
  (fn [render-fn]
    (fn [component value opts]
      (render-fn component
                 value
                 (-> opts
                     (assoc ::value value)
                     (update :cljfx.opt/tag->component-lifecycle
                             add-tag->component-lifecycle-fn
                             #(when (fn? %) exposed-value-fn-lifecycle)))))))