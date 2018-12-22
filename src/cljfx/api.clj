(ns cljfx.api
  (:require [cljfx.app :as app]
            [cljfx.component :as component]
            [cljfx.middleware :as middleware]
            [cljfx.platform :as platform]
            [cljfx.render :as render])
  (:import [javafx.application Platform]
           [javafx.embed.swing JFXPanel]))

(JFXPanel.)

(Platform/setImplicitExit false)

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
   (render/create desc opts)))

(defn advance-component
  ([component desc]
   (advance-component component desc {}))
  ([component desc opts]
   (render/advance component desc opts)))

(defn delete-component
  ([component]
   (delete-component component {}))
  ([component opts]
   (render/delete component opts)))

(defn instance [component]
  (component/instance component))

(defn create-app
  ([]
   (create-app identity))
  ([middleware]
   (app/create middleware)))

(defn mount-app [*ref app]
  (app/mount *ref app))

(defn wrap-add-map-event-handler [handler]
  (middleware/add-map-event-handler handler))

(defn wrap-map-value [f]
  (middleware/map-value f))

(defn wrap-map-opts [f]
  (middleware/map-opts f))

(defn wrap-expose-value []
  (middleware/expose-value))
