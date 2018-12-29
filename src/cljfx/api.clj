(ns cljfx.api
  (:require [cljfx.app :as app]
            [cljfx.component :as component]
            [cljfx.defaults :as defaults]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform])
  (:import [javafx.application Platform]
           [javafx.embed.swing JFXPanel]))

(JFXPanel.)

(Platform/setImplicitExit false)

(defn fx-tag->lifecycle [tag]
  (fx/tag->lifecycle tag))

(defn fn-tag->lifecycle [tag]
  (defaults/fn-tag->lifecycle tag))

(defn fn-tag->lifecycle-with-context [tag]
  (when (fn? tag) lifecycle/hiccup-fn-with-context->hiccup))

(def wrap-set-desc-as-context
  (fn [lifecycle]
    (lifecycle/wrap-desc-as-context lifecycle)))

(defn wrap-map-desc [f]
  (fn [lifecycle]
    (lifecycle/wrap-map-desc lifecycle f)))

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
   (lifecycle/create lifecycle/hiccup desc (defaults/fill-opts opts))))

(defn advance-component
  ([component desc]
   (advance-component component desc {}))
  ([component desc opts]
   (lifecycle/advance lifecycle/hiccup component desc (defaults/fill-opts opts))))

(defn delete-component
  ([component]
   (delete-component component {}))
  ([component opts]
   (lifecycle/delete lifecycle/hiccup component (defaults/fill-opts opts))))

(defn instance [component]
  (component/instance component))

(defn create-app [& {:keys [middleware opts]
                     :or {middleware identity
                          opts {}}}]
  (app/create middleware (defaults/fill-opts opts)))

(defn mount-app [*ref app]
  (app/mount *ref app))
