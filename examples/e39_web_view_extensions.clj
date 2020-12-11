(ns e39-web-view-extensions
  (:require [cljfx.api :as fx]
            [cljfx.ext.web-view :as fx.ext.web-view])
  (:import [javafx.scene.web WebEvent]))

;; this example shows how to use web view extensions that provide access to
;; WebEngine object that is used for obtaining web page information and
;; controlling it

(def *state
  (atom
    {:title nil
     :status nil}))

(defn view [{:keys [title status]}]
  {:fx/type :stage
   :showing true
   :title (str title)
   :scene
     {:fx/type :scene
      :root
        {:fx/type :v-box
         :children
           [{:fx/type fx.ext.web-view/with-engine-props
             :desc {:fx/type :web-view}
             :props {:url "https://github.com/cljfx/cljfx"
                     :on-title-changed #(swap! *state assoc :title %)
                     :on-status-changed #(swap! *state assoc :status (.getData ^WebEvent %))}}
            {:fx/type :label
             :text (str status)}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #'view)))

(fx/mount-renderer *state renderer)