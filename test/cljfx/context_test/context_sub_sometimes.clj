(ns cljfx.context-test.context-sub-sometimes
  (:require [cljfx.api :as fx]))

;; Event handler

(defmulti handler :event/type)

(defmethod handler ::clicked
  [{:keys [fx/context id] :as m}]
  {:context (-> context
                (fx/swap-context update ::clicked (fnil inc 0))
                (fx/swap-context update ::clicked-neg (fnil dec 0)))})

(defmethod handler ::reset
  [{:keys [fx/context id] :as m}]
  {:context (fx/swap-context context assoc ::clicked 0 ::clicked-neg 0)})

;; Views

(defn button-text [context]
  (let [clicked (or (fx/sub context ::clicked) 0)]
    (if (< 2 clicked)
      [clicked (fx/sub context ::clicked-neg)]
      clicked)))

(defn view [{:keys [fx/context] :as m}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 600
   :height 500
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children
                  [{:fx/type :button
                    :text (str "Reset")
                    :on-action {:event/type ::reset}}
                   {:fx/type :button
                    :text (str "Clicked: " (fx/sub context button-text))
                    :on-action {:event/type ::clicked}}]}}})

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
