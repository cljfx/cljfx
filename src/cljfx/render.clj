(ns cljfx.render
  (:require [cljfx.component :as component]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.lifecycle.fn :as lifecycle.fn]))

(defn- no-op [_])

(defn- tag->component-lifecycle [tag opts]
  (or (fx/tag->lifecycle tag)
      ((:cljfx.opt/tag->component-lifecycle opts no-op) tag)
      (when (fn? tag) lifecycle.fn/component)
      (throw (ex-info "Don't know how to get component lifecycle from tag"
                      {:tag tag}))))

(extend-protocol lifecycle/Lifecycle
  nil
  (create [_ desc opts]
    (let [lifecycle (-> desc first (tag->component-lifecycle opts))]
      (vary-meta (lifecycle/create lifecycle desc opts)
                 assoc
                 :cljfx/desc desc
                 `component/lifecycle (constantly lifecycle))))
  (advance [this component new-desc opts]
    (cond
      (and (nil? component) (nil? new-desc))
      nil

      (nil? component)
      (lifecycle/create this new-desc opts)

      (nil? new-desc)
      (lifecycle/delete this component opts)

      (not (identical? (component/lifecycle component)
                       (tag->component-lifecycle (first new-desc) opts)))
      (do (lifecycle/delete this component opts)
          (lifecycle/create this new-desc opts))

      :else
      (let [lifecycle (component/lifecycle component)]
        (vary-meta (lifecycle/advance lifecycle component new-desc opts)
                   assoc :cljfx/desc new-desc))))
  (delete [_ component opts]
    (lifecycle/delete (component/lifecycle component) component opts)))

(defn create [desc opts]
  (lifecycle/create-component desc opts))

(defn advance [component new-desc opts]
  (lifecycle/advance-component component new-desc opts))

(defn delete [component opts]
  (lifecycle/delete-component component opts))