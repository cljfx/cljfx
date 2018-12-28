(ns e06-pagination
  (:require [cljfx.api :as cljfx]))

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage {:showing true}
     [:scene
      [:pagination
       {:page-count 10
        :current-page-index 4
        :page-factory (fn [i]
                        [:label (str "This is a page " i)])}]]]))
