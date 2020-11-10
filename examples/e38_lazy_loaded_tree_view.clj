(ns e38-lazy-loaded-tree-view
  (:require [cljfx.api :as fx]))

;; Load tree items lazily by triggering loading when item is expanded

(def *state
  (atom {::expanded #{} ;; expanded ids
         ::tree {}})) ;; id -> children

(defmulti handle :event/type)

(defmethod handle ::on-expanded-changed [{:keys [id fx/event]}]
  (swap!
    *state
    #(-> %
         (update ::expanded (if event conj disj) id)
         (cond-> (and event (not (get-in % [::tree id])))
           (assoc-in [::tree id]
                     ;; This is "lazy loading". Note: swap! fn might be retried,
                     ;; don't do side effects here in your app
                     (random-sample 0.5 (map conj (repeat id) (range 5))))))))

(defn root-view [{::keys [expanded tree]}]
  (let [->desc (fn ->desc [id]
                 {:fx/type :tree-item
                  :value id
                  :expanded (contains? expanded id)
                  :on-expanded-changed {:event/type ::on-expanded-changed :id id}
                  :children (if (or (contains? expanded id) (tree id))
                              (map ->desc (tree id))
                              ;; this is a dummy tree item that exists to make
                              ;; its parent show as expandable:
                              [{:fx/type :tree-item}])})]
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :tree-view
                    :root (->desc [::root])}}}))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #'root-view)
    :opts {:fx.opt/map-event-handler handle}))

(fx/mount-renderer *state renderer)