(ns cljfx.context-test.context-dirty-entry-updates-key-deps
  (:require [cljfx.api :as fx]))

;; Event handler

(defmulti handler :event/type)

(defmethod handler ::more-buttons
  [{:keys [fx/context id] :as m}]
  {:context (fx/swap-context context update ::ids (fnil conj []) (gensym :id))})

(defmethod handler ::less-buttons
  [{:keys [fx/context] :as m}]
  {:context (-> context
                (fx/swap-context update ::ids
                                 #(or (when (seq %) (pop %)) []))
                ; clean up state
                (fx/swap-context update ::clicked
                                 dissoc (peek (fx/sub context ::ids))))})

(defmethod handler ::clicked
  [{:keys [fx/context id] :as m}]
  {:context (fx/swap-context context update-in [::clicked id] (fnil inc 0))})

;; Views

(defn buttons [{:keys [fx/context]}]
  (let [clicked (fx/sub context ::clicked)]
    {:fx/type :scroll-pane
     :fit-to-width true
     :fit-to-height true
     :content
     {:fx/type :h-box
      :children (mapv (fn [id]
                        {:fx/type :button
                         :text (str "x" (get clicked id 0))
                         :on-action {:event/type ::clicked
                                     :id id}})
                      (fx/sub context ::ids))}}))

(defn sum-buttons [context]
  (reduce #(let [clicked (fx/sub context ::clicked)]
             (+ %1 (get clicked %2 0)))
          0
          (fx/sub context ::ids)))

(defn view [{:keys [fx/context] :as m}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 600
   :height 500
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children
                  [{:fx/type :h-box
                    :children [{:fx/type :button
                                :on-action {:event/type ::less-buttons}
                                :text (str "Less buttons")}
                               {:fx/type :button
                                :on-action {:event/type ::more-buttons}
                                :text (str "More buttons")}]}
                   {:fx/type :label
                    :text (str "Sum: " (fx/sub context sum-buttons))}
                   {:fx/type buttons}]}}})

;; Main app

(declare *context app)

(when (and (.hasRoot #'*context)
           (.hasRoot #'app))
  (fx/unmount-renderer *context (:renderer app)))

(def *context
  (atom (fx/create-context {})))

(def app
  (fx/create-app *context
    :event-handler handler
    :desc-fn (fn [_]
               {:fx/type view})))
