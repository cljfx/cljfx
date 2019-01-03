(ns cljfx.api
  (:require [cljfx.app :as app]
            [cljfx.component :as component]
            [cljfx.defaults :as defaults]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform])
  (:import [javafx.application Platform]))

(defonce initialized
  (try
    (Platform/startup (fn []))
    :initialized
    (catch IllegalStateException _
      :already-initialized)))

(Platform/setImplicitExit false)

(defn keyword->lifecycle [fx-type]
  (fx/keyword->lifecycle fx-type))

(defn fn->lifecycle [fx-type]
  (defaults/fn->lifecycle fx-type))

(defn fn->lifecycle-with-context [fx-type]
  (when (fn? fx-type) lifecycle/dynamic-fn-with-context->dynamic))

(def wrap-set-desc-as-context
  (fn [lifecycle]
    (lifecycle/wrap-desc-as-context lifecycle)))

(defn wrap-map-desc [f & args]
  (fn [lifecycle]
    (apply lifecycle/wrap-map-desc lifecycle f args)))

(defmacro on-fx-thread
  "Execute body (in implicit do) on fx thread

  Returns derefable with result of last expression of body. If current thread is already
  fx thread, executes body immediately"
  [& body]
  `(platform/on-fx-thread ~@body))

(defn create-component
  ([desc]
   (create-component desc {}))
  ([desc opts]
   (lifecycle/create lifecycle/dynamic desc (defaults/fill-opts opts))))

(defn advance-component
  ([component desc]
   (advance-component component desc {}))
  ([component desc opts]
   (lifecycle/advance lifecycle/dynamic component desc (defaults/fill-opts opts))))

(defn delete-component
  ([component]
   (delete-component component {}))
  ([component opts]
   (lifecycle/delete lifecycle/dynamic component (defaults/fill-opts opts))))

(defn instance [component]
  (component/instance component))

(defn create-app [& {:keys [middleware opts]
                     :or {middleware identity
                          opts {}}}]
  (app/create middleware (defaults/fill-opts opts)))

(defn mount-app [*ref app]
  (app/mount *ref app))
