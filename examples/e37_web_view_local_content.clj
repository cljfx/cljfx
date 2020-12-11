(ns e37-web-view-local-content
  (:require [cljfx.api :as fx]
            [cljfx.prop :as fx.prop]
            [cljfx.mutator :as fx.mutator]
            [cljfx.lifecycle :as fx.lifecycle])
  (:import [javafx.scene.web WebView]))

;; Short snippet demonstrating how to display local content in a WebView
;; with a custom prop

(def ext-with-html
  (fx/make-ext-with-props
    {:html (fx.prop/make
             (fx.mutator/setter #(.loadContent (.getEngine ^WebView %1) %2))
             fx.lifecycle/scalar)}))

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type ext-with-html
                    :props {:html "<h1>hello html!</h1>"}
                    :desc {:fx/type :web-view}}}}))
