(ns e46-menu-items
  (:require [cljfx.api :as fx]))

;; Simple example with menu items

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :children [{:fx/type :menu-bar
                                :menus [{:fx/type :menu
                                         :text "File"
                                         :items [{:fx/type :menu-item
                                                  :accelerator [:shortcut :o]
                                                  :on-action #(println "Open" %)
                                                  :text "Open"}
                                                 {:fx/type :separator-menu-item}
                                                 {:fx/type :menu-item
                                                  :accelerator [:shortcut :comma]
                                                  :on-action #(println "Preferences" %)
                                                  :text "Preferences"}
                                                 {:fx/type :menu-item
                                                  :accelerator [:shortcut :q]
                                                  :on-action #(println "Quit" %)
                                                  :text "Quit"}]}]}
                               {:fx/type :label
                                :padding 50
                                :text "Hello world!"}]}}}))