(ns cljfx.app
  (:require [cljfx.platform :as platform]
            [cljfx.render :as render]))

(defn- perform-render
  "Re-renders app on fx thread

  Since advancing is a mutating operation on dom, it can't be in `swap!` which
  may retry it. During advancing new render request may arrive, in that case we
  immediately re-render app to always view it's actual state"
  [*app]
  (let [{:keys [desc *component render-fn]} @*app
        _ (vreset! *component (render-fn @*component desc {}))
        new-app (swap! *app #(assoc % :request-rendering (not= desc (:desc %))))]
    (when (:request-rendering new-app)
      (recur *app))))

(defn- request-render [*app desc]
  (let [[old _] (swap-vals! *app assoc :desc desc :request-rendering true)]
    (when-not (:request-rendering old)
      (platform/on-fx-thread (perform-render *app)))))

(defn create [middleware]
  (let [*app (atom {:*component (volatile! nil)
                    :render-fn (middleware render/advance)
                    :request-rendering false})]
    (fn [desc]
      (request-render *app desc)
      nil)))

(defn mount [*ref app]
  (add-watch *ref [`mount app] #(app %4))
  (app @*ref))
