(ns cljfx.renderer
  "Part of a public API"
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform]))

(set! *warn-on-reflection* true)

(defn- complete-rendering [renderer desc component]
  (let [with-component (assoc renderer :component component)]
    (if (= desc (:desc renderer))
      (dissoc with-component :request)
      (assoc with-component :request (promise)))))

(defn- perform-render
  "Re-renders component on fx thread

  Since advancing is a mutating operation on dom, it can't be in `swap!` which
  may retry it. During advancing new render request may arrive, in that case we
  enqueue another re-render component to not lock JavaFX application thread"
  [*renderer]
  (let [{:keys [desc component render-fn request]} @*renderer
        [new-component ^Exception exception] (try
                                               [(render-fn component desc) nil]
                                               (catch Exception e
                                                 [component e]))
        new-renderer (swap! *renderer complete-rendering desc new-component)]
    (some-> exception .printStackTrace)
    (deliver request new-component)
    (when (:request new-renderer)
      (platform/run-later (perform-render *renderer)))))

(defn- or-new-promise [x]
  (or x (promise)))

(defn- request-rendering-with-desc [renderer desc]
  (-> renderer
      (assoc :desc desc)
      (update :request or-new-promise)))

(defn- request-render [*renderer desc]
  (let [[old new] (swap-vals! *renderer request-rendering-with-desc desc)]
    (when-not (:request old)
      (platform/run-later (perform-render *renderer)))
    (:request new)))

(defn- render-component
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
        *renderer (atom {:component nil
                         :render-fn #(render-component lifecycle %1 %2 opts)})]
    (fn render
      ([]
       (let [desc (:desc @*renderer)]
         @(render nil)
         (render desc)))
      ([desc]
       (request-render *renderer desc)))))

(defn mount [*ref renderer]
  (add-watch *ref [`mount renderer] #(renderer %4))
  (renderer @*ref))
