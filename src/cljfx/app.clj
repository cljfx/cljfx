(ns cljfx.app
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform]))

(set! *warn-on-reflection* true)

(defn complete-rendering [app desc component]
  (cond-> app
    :always (assoc :component component)
    (= desc (:desc app)) (dissoc :request)))

(defn- perform-render
  "Re-renders app on fx thread

  Since advancing is a mutating operation on dom, it can't be in `swap!` which
  may retry it. During advancing new render request may arrive, in that case we
  immediately re-render app to always view it's actual state"
  [*app]
  (let [{:keys [desc component render-fn request]} @*app
        [new-component ^Exception exception] (try
                                               [(render-fn component desc) nil]
                                               (catch Exception e
                                                 [component e]))
        new-app (swap! *app complete-rendering desc new-component)]
    (some-> exception .printStackTrace)
    (if (:request new-app)
      (recur *app)
      (deliver request new-component))))

(defn- or-new-promise [x]
  (or x (promise)))

(defn- request-rendering-with-desc [app desc]
  (-> app
      (assoc :desc desc)
      (update :request or-new-promise)))

(defn- request-render [*app desc]
  (let [[old new] (swap-vals! *app request-rendering-with-desc desc)]
    (when-not (:request old)
      (platform/run-later (perform-render *app)))
    (:request new)))

(defn- render-app-component
  "Advance rendered component with special semantics for nil (meaning absence)

  This allows to create, advance and delete components in single function"
  [lifecycle component desc opts]
  (cond
    (and (nil? component) (nil? desc))
    nil

    (nil? component)
    (lifecycle/create lifecycle desc opts)

    (nil? desc)
    (do (lifecycle/delete lifecycle component opts) nil)

    :else
    (lifecycle/advance lifecycle component desc opts)))

(defn create [middleware opts]
  (let [lifecycle (middleware lifecycle/root)
        *app (atom {:component nil
                    :render-fn #(render-app-component lifecycle %1 %2 opts)})]
    (fn app
      ([]
       (let [desc (:desc @*app)]
         @(app nil)
         (app desc)))
      ([desc]
       (request-render *app desc)))))

(defn mount [*ref app]
  (add-watch *ref [`mount app] #(app %4))
  (app @*ref))
