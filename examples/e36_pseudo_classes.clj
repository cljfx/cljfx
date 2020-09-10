(ns e36-pseudo-classes
  (:require [cljfx.css :as css]
            [cljfx.api :as fx]))

(def style
  (css/register ::style
    {":info" {:-fx-background-color "#bbf"
              ":hover" {:-fx-background-color "#ddf"}}
     ":warning" {:-fx-background-color "#f80"
                 ":hover" {:-fx-background-color "#fc6"}}
     ":error" {:-fx-background-color "#f00"
               ":hover" {:-fx-background-color "#f88"}}
     ":size-s" {:-fx-padding 5
                :-fx-font-size 13}
     ":size-m" {:-fx-padding 10
                :-fx-font-size 16}
     ":size-l" {:-fx-padding 20
                :-fx-font-size 20}}))

(def *state
  (atom {:type :info
         :size :size-m}))

(defn control [{:keys [value items key label]}]
  {:fx/type :v-box
   :spacing 2
   :children [{:fx/type :label
               :text label}
              {:fx/type :combo-box
               :value value
               :on-value-changed #(swap! *state assoc key %)
               :items items}]})

(defn view [{:keys [type size]}]
  {:fx/type :stage
   :showing true
   :width 400
   :height 300
   :scene {:fx/type :scene
           :stylesheets [(::css/url style)]
           :root {:fx/type :v-box
                  :padding 20
                  :alignment :center
                  :children [{:fx/type :h-box
                              :v-box/vgrow :always
                              :alignment :center
                              :children [{:fx/type :label
                                          :pseudo-classes #{type size}
                                          :text (str type " " size)}]}
                             {:fx/type :h-box
                              :alignment :center
                              :spacing 10
                              :children [{:fx/type control
                                          :value size
                                          :items [:size-s :size-m :size-l]
                                          :key :size
                                          :label "Size"}
                                         {:fx/type control
                                          :value type
                                          :items [:info :warning :error]
                                          :key :type
                                          :label "Type"}]}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #'view)))

(fx/mount-renderer *state renderer)