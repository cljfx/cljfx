(ns e35-popup
  (:require [cljfx.api :as fx]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.lifecycle :as lifecycle])
  (:import [javafx.stage Popup]
           [javafx.scene Node]))

(def *state (atom false))

(defn- show-popup! [_]
  (reset! *state true))

(defn- hide-popup! [_]
  (reset! *state false))

(def popup-width 300)

(def ext-with-shown-on
  (fx/make-ext-with-props
    {:shown-on (prop/make
                 (mutator/adder-remover
                   (fn [^Popup popup ^Node node]
                     (let [bounds (.getBoundsInLocal node)
                           node-pos (.localToScreen node (* 0.5 (.getWidth bounds)) 0.0)]
                       (.show popup node
                              (- (.getX node-pos) (* 0.5 popup-width))
                              (.getY node-pos))))
                   (fn [^Popup popup _]
                     (.hide popup)))
                 lifecycle/dynamic)}))

(defn view [show-popup]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type fx/ext-let-refs
                  :refs {::label {:fx/type :label
                                  :padding 20
                                  :on-mouse-entered show-popup!
                                  :on-mouse-exited hide-popup!
                                  :text "Hover me for popup!"}}
                  :desc {:fx/type fx/ext-let-refs
                         :refs {::popup {:fx/type ext-with-shown-on
                                         :props (when show-popup
                                                  {:shown-on {:fx/type fx/ext-get-ref :ref ::label}})
                                         :desc {:fx/type :popup
                                                :anchor-location :window-bottom-left
                                                :auto-hide true
                                                :auto-fix false
                                                :on-hidden hide-popup!
                                                :content [{:fx/type :label
                                                           :padding 20
                                                           :min-width popup-width
                                                           :max-width popup-width
                                                           :pref-width popup-width
                                                           :style {:-fx-background-color :white}
                                                           :effect {:fx/type :drop-shadow}
                                                           :text "Hello!"}]}}}
                         :desc {:fx/type fx/ext-get-ref :ref ::label}}}}})

(def renderer
  (fx/create-renderer :middleware (fx/wrap-map-desc #'view)))

(fx/mount-renderer *state renderer)
