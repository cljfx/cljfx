(ns cljfx.app
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform]))

(defn- perform-render
  "Re-renders app on fx thread

  Since advancing is a mutating operation on dom, it can't be in `swap!` which
  may retry it. During advancing new render request may arrive, in that case we
  immediately re-render app to always view it's actual state"
  [*app]
  (let [{:keys [desc *component render-fn]} @*app
        _ (vreset! *component (render-fn @*component desc))
        new-app (swap! *app #(assoc % :request-rendering (not= desc (:desc %))))]
    (when (:request-rendering new-app)
      (recur *app))))

(defn- request-render [*app desc]
  (let [[old _] (swap-vals! *app assoc :desc desc :request-rendering true)]
    (when-not (:request-rendering old)
      (platform/on-fx-thread (perform-render *app)))))

(defn- render-app-component
  "Advance rendered component with special semantics for nil (meaning absence)

  This allows to create, advance and delete components in single function"
  [component desc opts]
  (cond
    (and (nil? component) (nil? desc))
    nil

    (nil? component)
    (lifecycle/create lifecycle/dynamic-hiccup desc opts)

    (nil? desc)
    (do (lifecycle/delete lifecycle/dynamic-hiccup component opts) nil)

    :else
    (lifecycle/advance lifecycle/dynamic-hiccup component desc opts)))

(defn create [middleware opts]
  (let [render-fn (middleware render-app-component)
        *app (atom {:*component (volatile! nil)
                    :render-fn #(render-fn %1 %2 opts)
                    :request-rendering false})]
    (fn [desc]
      (request-render *app desc)
      nil)))

(defn mount [*ref app]
  (add-watch *ref [`mount app] #(app %4))
  (app @*ref))
