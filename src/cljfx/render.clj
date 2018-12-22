(ns cljfx.render
  (:require [cljfx.component :as component]
            [cljfx.impl :as impl]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.lifecycle.fn :as lifecycle.fn]))

(defn- no-op [_])

(defn- tag->component-lifecycle [tag]
  (or (fx/tag->lifecycle tag)
      ((:cljfx.opt/tag->component-lifecycle impl/*opts* no-op) tag)
      (when (fn? tag) lifecycle.fn/component)
      (throw (ex-info "Don't know how to get component lifecycle from tag"
                      {:tag tag}))))

(extend-protocol lifecycle/Lifecycle
  nil
  (create [_ desc]
    (let [tag (first desc)
          ret (-> tag
                  tag->component-lifecycle
                  (lifecycle/create desc)
                  (vary-meta assoc :cljfx/desc desc))]
      (when-let [f (:cljfx/on-create (meta desc))]
        (f ret))
      ret))
  (advance [this component new-desc]
    (cond
      (and (nil? component) (nil? new-desc))
      nil

      (nil? component)
      (lifecycle/create this new-desc)

      (nil? new-desc)
      (lifecycle/delete this component)

      (not= (component/tag component) (first new-desc))
      (do (lifecycle/delete this component)
          (lifecycle/create this new-desc))

      :else
      (let [tag (first new-desc)]
        (-> tag
            tag->component-lifecycle
            (lifecycle/advance component new-desc)
            (vary-meta assoc :cljfx/desc new-desc)))))
  (delete [_ component]
    (-> component component/tag tag->component-lifecycle (lifecycle/delete component))
    nil))

(defmacro with-opts [opts & body]
  `(binding [impl/*opts* ~opts]
     ~@body))

(defn create [desc opts]
  (with-opts opts
    (lifecycle/create-component desc)))

(defn advance [component new-desc opts]
  (with-opts opts
    (lifecycle/advance-component component new-desc)))

(defn delete [component opts]
  (with-opts opts
    (lifecycle/delete-component component)))