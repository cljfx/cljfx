(ns e07-panes
  (:require [cljfx.api :as cljfx]))

(def anchor-pane
  [:anchor-pane
   {:children [^{:left 10 :bottom 10}
               [:label {:text "bottom-left"}]

               ^{:top 10 :right 10}
               [:label {:text "top-right"}]

               ^{:top 100 :bottom 100 :left 100 :right 100}
               [:label
                {:style {:-fx-background-color :lightgray
                         :-fx-alignment :center}
                 :text "Try resizing window too!"}]]}])

(def border-pane
  [:border-pane
   {:top ^{:alignment :center :margin 10} [:label {:text "top header"}]
    :left ^{:margin 10} [:label {:text "left sidebar"}]
    :right ^{:margin 10} [:label {:text "right sidebar"}]
    :center ^{:margin 10} [:label {:text "center content"}]
    :bottom ^{:margin 10} [:label {:text "bottom footer"}]}])

(def flow-pane
  [:flow-pane
   {:vgap 5
    :hgap 5
    :padding 5
    :children (repeat 100 [:rectangle {:width 25 :height 25}])}])

(def grid-pane
  [:grid-pane
   {:children (concat
                (for [i (range 16)]
                  ^{:column i :row i :hgrow :always :vgrow :always}
                  [:label {:text "boop"}])
                [^{:row 2 :column 3 :column-span 2}
                 [:label {:text "I am a long label spanning 2 columns"}]])}])

(def h-box
  [:h-box
   {:spacing 5
    :children [[:label {:text "just label"}]
               ^{:hgrow :always}
               [:label
                {:max-width Double/MAX_VALUE
                 :style {:-fx-background-color :lightgray}
                 :text "expanded label"}]
               ^{:margin 100}
               [:label {:text "label with big margin"}]]}])

(def stack-pane
  [:stack-pane
   {:children [[:rectangle {:width 200 :height 200 :fill :lightgray}]
               ^{:alignment :bottom-left :margin 5}
               [:label {:text "stacked label"}]
               ^{:alignment :top-right :margin 5}
               [:text-field {:max-width 300 :text "Text field in top-right corner"}]]}])

(defn- tile-image [url]
  [:image-view
   {:image {:url url
            :requested-width 310
            :preserve-ratio true
            :background-loading true}}])

(def tile-pane
  [:scroll-pane
   {:fit-to-width true
    :content [:tile-pane
              {:pref-columns 3
               :hgap 5
               :vgap 5
               :children [^{:alignment :bottom-center}
                          [tile-image "https://i.imgur.com/oy91jyx.gif"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.imgur.com/B4DdoER.png"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.imgur.com/mQOeSe5.png"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/6906qzxo55711.png"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/810g0l3sgis01.gif"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/rpkzzc0awr411.gif"]
                          ^{:alignment :bottom-center}
                          [tile-image "http://i.imgur.com/G3dVZpk.jpg"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/k4hax2x5yyhy.png"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.imgur.com/PRxRkne.png"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/zusrb3sxsz211.gif"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/fagm2fhxv1yz.gif"]
                          ^{:alignment :bottom-center}
                          [tile-image "https://i.redd.it/w49wc60kys401.gif"]]}]}])

(def v-box
  [:v-box
   {:spacing 5
    :fill-width true
    :alignment :top-center
    :children [[:label {:text "just label"}]
               ^{:vgrow :always}
               [:label
                {:style {:-fx-background-color :lightgray}
                 :max-height Double/MAX_VALUE
                 :max-width Double/MAX_VALUE
                 :text "expanded label"}]]}])

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage
     {:showing true
      :title "Pane examples"
      :scene [:scene
              {:root [:tab-pane
                      {:pref-width 960
                       :pref-height 540
                       :tabs [[:tab
                               {:text "Anchor Pane"
                                :closable false
                                :content anchor-pane}]
                              [:tab
                               {:text "Border Pane"
                                :closable false
                                :content border-pane}]
                              [:tab
                               {:text "Flow Pane"
                                :closable false
                                :content flow-pane}]
                              [:tab
                               {:text "Grid Pane"
                                :closable false
                                :content grid-pane}]
                              [:tab
                               {:text "HBox"
                                :closable false
                                :content h-box}]
                              [:tab
                               {:text "Stack Pane"
                                :closable false
                                :content stack-pane}]
                              [:tab
                               {:text "Tile Pane"
                                :closable false
                                :content tile-pane}]
                              [:tab
                               {:text "VBox"
                                :closable false
                                :content v-box}]]}]}]}]))