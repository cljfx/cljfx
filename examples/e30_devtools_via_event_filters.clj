; Author: Ambrose Bonnaire-Sergeant
(ns e30-devtools-via-event-filters
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache])
  (:import [javafx.scene.input MouseEvent]
           [javafx.scene Node]
           [javafx.event Event]))

;; Setting an event filter on a node allows that node to
;; intercept events intended for its children.
;; 
;; Add `:event-filter` to any Node
;; to register a catch-all event filter.
;;
;; This example highlights hovered nodes by
;; setting an event filter on the root node that intercepts
;; MouseEvent's sent to inner nodes.

(set! *warn-on-reflection* true)

(def *context
  (atom (fx/create-context
          {:current-node nil}
          cache/lru-cache-factory)))

; [(U nil Node) Event CSSClass -> (U nil node)]
(defn devtools-highlight-filter
  "Takes the previously highlighted node (or nil), 
  an Event (via event filter), and the CSS class
  to apply to highlighted nodes.

  Returns the current highlighted node (or nil)
  after highlighting it (and removing highlighting
  on the previous node)."
  [^Node hovered-node ^Event event css-class]
  {:pre [(string? css-class)]}
  (when (instance? MouseEvent event)
    (let [^Node target (.getTarget event)
          event-type (.getEventType event)]
      (when (instance? Node target)
        (if (#{MouseEvent/MOUSE_EXITED
               MouseEvent/MOUSE_EXITED_TARGET}
              event-type)
          (let [_ (-> target .getStyleClass (.remove css-class))]
            (when (not= hovered-node target)
              hovered-node))
          (let [_ (when (and hovered-node (not= hovered-node target))
                    (-> hovered-node .getStyleClass (.remove css-class)))
                _ (when (not= hovered-node target)
                    (when-not (-> target .getStyleClass (.contains css-class))
                      (-> target .getStyleClass (.add css-class))))]
            target))))))

(def mouse-over-css-class "mouse-over-highlight")

(defmulti handle-event :event/type)
(defmethod handle-event ::on-event-filter [{:keys [fx/context fx/event]}]
  {:context (fx/swap-context context update :current-node
                             devtools-highlight-filter
                             event
                             mouse-over-css-class)})

(defn root-view [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :width 600
   :height 500
   :scene {:fx/type :scene
           :stylesheets #{"devtools.css"}
           :root
           {:fx/type :v-box
            ;; add an event filter to the root node
            :event-filter {:event/type ::on-event-filter
                           ; because we are changing observable lists in
                           ; the event handler
                           :fx/sync true}
            ;; the UI
            :children
            [{:fx/type :label
              :text (str "Current node: " (some-> (fx/sub-val context :current-node)
                                                  class
                                                  .getSimpleName))}
             {:fx/type :label
              :text (str "Has CSS classes: " (some-> ^Node (fx/sub-val context :current-node)
                                                     .getStyleClass
                                                     vec))}
             ; mouse over these nodes to highlight them
             {:fx/type :split-pane
              :divider-positions [0.5]
              :items [{:fx/type :v-box
                       :padding 50
                       :children [{:fx/type :split-pane
                                   :divider-positions [0.5]
                                   :items [{:fx/type :v-box
                                            :padding 50
                                            :children [{:fx/type :h-box
                                                        :children [{:fx/type :label
                                                                    :text "left 1"}]}
                                                       {:fx/type :label
                                                        :text "left 2"}]}
                                           {:fx/type :h-box
                                            :padding 50
                                            :children [{:fx/type :label
                                                        :text "right"}]}]}
                                  {:fx/type :label
                                   :text "left 2"}]}
                      {:fx/type :h-box
                       :padding 50
                       :children [{:fx/type :label
                                   :text "right"}]}]}]}}})

(def app
  (fx/create-app *context
    :event-handler handle-event
    :desc-fn (fn [_]
               {:fx/type root-view})))
