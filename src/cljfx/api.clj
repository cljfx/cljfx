(ns cljfx.api
  (:require [cljfx.app :as app]
            [cljfx.component :as component]
            [cljfx.defaults :as defaults]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.middleware :as middleware]
            [cljfx.platform :as platform])
  (:import [javafx.application Platform]
           [javafx.embed.swing JFXPanel]))

(JFXPanel.)

(Platform/setImplicitExit false)

(def default-opts
  defaults/opts)

(defn default-tag->lifecycle [tag]
  (defaults/tag->lifecycle tag))

(defn fx-tag->lifecycle [tag]
  (fx/tag->lifecycle tag))

(defn fn-tag->lifecycle [tag]
  (defaults/fn-tag->lifecycle tag))

(defn fn-tag->exposed-lifecycle [tag]
  (middleware/fn-tag->exposed-lifecycle tag))

(defn default-map-event-handler [event]
  (defaults/map-event-handler event))

(defmacro on-fx-thread
  "Execute body (in implicit do) on fx thread

  Returns derefable with result of last expression of body. If current thread is already
  fx thread, executes body immediately"
  [& body]
  `(platform/on-fx-thread ~@body))

(defn create-component
  ([desc]
   (create-component desc default-opts))
  ([desc opts]
   (lifecycle/create lifecycle/dynamic-hiccup desc opts)))

(defn advance-component
  ([component desc]
   (advance-component component desc default-opts))
  ([component desc opts]
   (lifecycle/advance lifecycle/dynamic-hiccup component desc opts)))

(defn delete-component
  ([component]
   (delete-component component default-opts))
  ([component opts]
   (lifecycle/delete lifecycle/dynamic-hiccup component opts)))

(defn instance [component]
  (component/instance component))

(defn create-app [& {:keys [middleware opts]
                     :or {middleware identity
                          opts default-opts}}]
  (app/create middleware opts))

(defn mount-app [*ref app]
  (app/mount *ref app))

(defn wrap-map-value [f]
  (middleware/map-value f))

(defn wrap-expose-value []
  (middleware/expose-value))
