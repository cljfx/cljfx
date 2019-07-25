(ns cljfx.context-test.empty-context
  (:require [cljfx.api :as fx]))

; minimal example of bug in context where adding
; a new entry to the root of the context does not
; trigger rerenderings.

; To reproduce:
; 1. Run this file.
; 2. Click the "Clicked x0" button a few times, it works.
; 3. Click the "Reset via assoc" button, again Step 2 works fine.
; 4. Click the "Reset via dissoc" button. Now, Step 2 skips "Clicked x1",
;    and "Clicked x0" is shown twice!

;; Initial State

(defn init-state [] {::clicked nil})

;; Views

(defn view [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 600
   :height 500
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :h-box
                              :children [{:fx/type :button
                                          :on-action {:event/type ::reset-assoc}
                                          :text (str "Reset via assoc")}
                                         {:fx/type :button
                                          :on-action {:event/type ::reset-dissoc}
                                          :text (str "Reset via dissoc")}]}
                             (let [clicked (fx/sub context ::clicked)]
                               {:fx/type :button
                                :on-action {:event/type ::clicked
                                            :clicked clicked}
                                :text (str "Clicked x" (or clicked 0))}) ]}}})

; Handlers

(defmulti handler :event/type)
(defmethod handler ::clicked
  [{:keys [fx/context clicked]}]
  {:context (fx/swap-context context update ::clicked (fnil inc 0))})

(defmethod handler ::reset-dissoc
  [{:keys [fx/context]}]
  {:context (fx/swap-context context dissoc ::clicked)})

(defmethod handler ::reset-assoc
  [{:keys [fx/context]}]
  {:context (fx/swap-context context assoc ::clicked nil)})

;; Main app

(declare *context app)

(when (and (.hasRoot #'*context)
           (.hasRoot #'app))
  (fx/unmount-renderer *context (:renderer app)))

(def *context
  (atom (fx/create-context (init-state))))

(def app
  (fx/create-app *context
    :event-handler handler
    :desc-fn (fn [_]
               {:fx/type view})))
