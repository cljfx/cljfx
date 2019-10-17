(ns e21-extension-lifecycles
  (:require [cljfx.api :as fx]
            [cljfx.ext.node :as fx.ext.node])
  (:import [javafx.scene Node]
           [javafx.animation TranslateTransition Interpolator]
           [javafx.util Duration]
           [javafx.scene.layout Region]))

(defn- animate-entrance [^Node node]
  (doto (TranslateTransition. (Duration/seconds 2) node)
    (.setFromY -100)
    (.setToY 0)
    (.setInterpolator Interpolator/EASE_OUT)
    (.play)))

;; Imperative escape hatch in declarative world: you can use ext-on-instance-lifecycle to
;; execute code when instance is created, replaced or deleted

(defn ext-on-instance-lifecycle-example [_]
  {:fx/type fx/ext-on-instance-lifecycle
   :on-created animate-entrance
   :desc {:fx/type :region
          :pref-width 20
          :pref-height 20
          :style {:-fx-background-color :red}}})

;; Use ext-instance-factory to just create an instance

(defn ext-instance-factory-example [_]
  {:fx/type fx/ext-instance-factory
   :create #(doto (Region.)
              (.setPrefWidth 20)
              (.setPrefHeight 20)
              (.setStyle "-fx-background-color: green;"))})

;; Use ext-let-refs and ext-get-ref to decouple lifecycles from scene graph. It allows to
;; have same exact component instances in different places

(defn ext-ref-examples [_]
  {:fx/type fx/ext-let-refs
   :refs {::button-a {:fx/type :button
                      :text "Press Alt+A to focus on me"}
          ::button-b {:fx/type :button
                      :text "Press Alt+B to focus on me"}}
   :desc {:fx/type :v-box
          :spacing 5
          :children [{:fx/type :label
                      :text "Mnemonic _A"
                      :mnemonic-parsing true
                      :label-for {:fx/type fx/ext-get-ref
                                  :ref ::button-a}}
                     {:fx/type fx/ext-get-ref
                      :ref ::button-a}
                     {:fx/type :label
                      :text "Mnemonic _B"
                      :mnemonic-parsing true
                      :label-for {:fx/type fx/ext-get-ref
                                  :ref ::button-b}}
                     {:fx/type fx/ext-get-ref
                      :ref ::button-b}]}})

;; Use make-ext-with-props to create extension lifecycle that allows specifying additional
;; props on a component if there is something missing. Some extension lifecycles for
;; commonly used functionality not covered by essential props are provided in cljfx.ext.*
;; namespaces

(defn ext-with-props-example [_]
  {:fx/type :h-box
   :padding 5
   :spacing 5
   :children [{:fx/type :label
               :text "Hover this circle for a tooltip →"}
              {:fx/type fx.ext.node/with-tooltip-props
               :props {:tooltip {:fx/type :tooltip :text "Hello there"}}
               :desc {:fx/type :circle
                      :radius 10}}]})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type fx/ext-many
     :desc [{:fx/type :stage
             :showing true
             :always-on-top true
             :scene {:fx/type :scene
                     :root {:fx/type :v-box
                            :alignment :center
                            :fill-width false
                            :padding 100
                            :children [{:fx/type ext-on-instance-lifecycle-example}
                                       {:fx/type ext-instance-factory-example}
                                       {:fx/type ext-ref-examples}
                                       {:fx/type ext-with-props-example}]}}}
            {:fx/type fx/ext-set-env
             :env {::label-text "Extra window thanks to `fx/ext-many`"}
             :desc {:fx/type :stage
                    :showing true
                    :x 0
                    :scene {:fx/type :scene
                            :root {:fx/type :v-box
                                   :padding 100
                                   :children [{:fx/type fx/ext-get-env
                                               :env {::label-text :text}
                                               :desc {:fx/type :label}}]}}}}]}))
